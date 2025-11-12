package com.service.frame.round.service

import com.service.frame.ad.service.AdQueueService
import com.service.frame.ad.repository.AdTaskRepository
import com.service.frame.member.repository.MemberRepository
import com.service.frame.order.repository.OrderRepository
import com.service.frame.round.dto.RoundCreateRequest
import com.service.frame.round.dto.RoundResponse
import com.service.frame.round.entity.Round
import com.service.frame.round.entity.RoundStatus
import com.service.frame.round.repository.RoundRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import java.io.File
import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Service
@Transactional
class RoundServiceImpl(
    private val roundRepository: RoundRepository,
    private val memberRepository: MemberRepository,
    private val orderRepository: OrderRepository,
    private val adQueueService: AdQueueService,
    private val adTaskRepository: AdTaskRepository,
    @PersistenceContext private val entityManager: EntityManager
) : RoundService {
    
    private val logger = LoggerFactory.getLogger(RoundServiceImpl::class.java)

    override fun createRound(request: RoundCreateRequest, createdById: Long): RoundResponse {
        val creator = memberRepository.findById(createdById).orElse(null) 
            ?: throw IllegalArgumentException("Member not found with id: $createdById")

        // 자동으로 postStartDate와 postEndDate 계산
        val postStartDate = request.postStartDate ?: request.endDate
        val postDurationDays = request.postDurationDays ?: 7
        val postEndDate = request.postEndDate ?: postStartDate.plusDays(postDurationDays.toLong())

        val round = Round(
            title = request.title,
            description = request.description,
            category = request.category,
            orderAmount = request.orderAmount,
            templateCost = request.templateCost,
            aiGenerationCost = request.aiGenerationCost,
            targetingPostingCost = request.targetingPostingCost,
            serverRentalCost = request.serverRentalCost,
            otherCosts = request.otherCosts,
            startDate = request.startDate,
            endDate = request.endDate,
            postStartDate = postStartDate,
            postEndDate = postEndDate,
            postDurationDays = postDurationDays,
            status = RoundStatus.PREPARING,
            maxParticipants = request.maxParticipants,
            createdBy = creator
        )

        val savedRound = roundRepository.save(round)
        
        // 라운드 번호 생성 (DB 함수 사용)
        val roundNumber = generateRoundNumber()
        savedRound.roundNumber = roundNumber
        
        // 프리미엄 활성 회원에 대해 광고 생성 작업을 큐에 추가
        try {
            val activeMembers = memberRepository.findActivePremiumMembers()
            logger.info("라운드 ${savedRound.id} 생성 완료. ${activeMembers.size}명의 프리미엄 활성 회원에 대해 광고 생성 작업을 시작합니다.")
            
            if (activeMembers.isNotEmpty()) {
                adQueueService.enqueueAdGenerationTasks(savedRound, activeMembers)
                logger.info("라운드 ${savedRound.id}에 대한 광고 생성 작업이 큐에 추가되었습니다.")
            } else {
                logger.warn("프리미엄 활성 회원이 없어 광고 생성 작업을 추가하지 않습니다.")
            }
        } catch (e: Exception) {
            logger.error("라운드 ${savedRound.id}의 광고 생성 작업 큐 추가 중 오류 발생", e)
            // 광고 생성 큐 추가 실패가 라운드 생성을 막지 않도록 함
        }
        
        return RoundResponse.from(savedRound, 0)
    }

    @Transactional(readOnly = true)
    override fun getRounds(status: RoundStatus?, pageable: Pageable): Page<RoundResponse> {
        val rounds = if (status != null) {
            roundRepository.findActiveRoundsOrderByCreatedAtDesc(status, pageable)
        } else {
            // 모든 상태의 라운드 조회
            roundRepository.findAllByOrderByCreatedAtDesc(pageable)
        }
        
        return rounds.map { round ->
            val currentParticipants = orderRepository.countByRoundId(round.id!!).toInt()
            RoundResponse.from(round, currentParticipants)
        }
    }

    @Transactional(readOnly = true)
    override fun getRoundById(id: Long): RoundResponse {
        val round = roundRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $id")
        
        val currentParticipants = orderRepository.countByRoundId(id).toInt()
        return RoundResponse.from(round, currentParticipants)
    }

    @Transactional(readOnly = true)
    override fun getRoundsByCreatedBy(memberId: Long, pageable: Pageable): Page<RoundResponse> {
        val rounds = roundRepository.findByCreatedByIdOrderByCreatedAtDesc(memberId, pageable)
        return rounds.map { round ->
            val currentParticipants = orderRepository.countByRoundId(round.id!!).toInt()
            RoundResponse.from(round, currentParticipants)
        }
    }

    @Transactional(readOnly = true)
    override fun getPostedAdsForMemberInRound(roundId: Long, memberId: Long): List<Map<String, Any>> {
        val query = """
            SELECT DISTINCT
                aa.id as assignmentId,
                at.id as adTaskId,
                at.ad_type as adType,
                at.web_url as webUrl,
                pm.company_name as publisherName,
                ap.content as postContent,
                ap.post_status as postStatus,
                ap.published_at as publishedAt,
                aa.created_at as createdAt
            FROM ad_tasks at
            INNER JOIN advertisement_assignments aa ON aa.ad_task_id = at.id
            LEFT JOIN advertisement_posts ap ON aa.id = ap.assignment_id
            INNER JOIN members pm ON aa.publisher_member_id = pm.id
            WHERE at.id IN (
                    SELECT o.ad_task_id FROM orders o
                    INNER JOIN order_payments op ON o.id = op.order_id
                    WHERE o.member_id = :memberId
                        AND op.payment_status = 'CONFIRMED'
                )
                AND at.round_id = :roundId
                AND at.task_status = 'COMPLETED'
                AND aa.publisher_member_id != :memberId
            ORDER BY aa.created_at DESC
        """.trimIndent()

        val resultList = entityManager.createNativeQuery(query)
            .setParameter("roundId", roundId)
            .setParameter("memberId", memberId)
            .resultList

        return resultList.map { result ->
            val row = result as Array<Any?>
            
            mapOf<String, Any>(
                "assignmentId" to (row[0] ?: 0),
                "adTaskId" to (row[1] ?: 0),
                "adType" to (row[2] ?: ""),
                "webUrl" to (row[3] ?: ""),
                "publisherName" to (row[4] ?: ""),
                "postContent" to (row[5] ?: ""),
                "postStatus" to (row[6] ?: ""),
                "publishedAt" to (row[7] ?: ""),
                "createdAt" to (row[8] ?: "")
            )
        }
    }

    @Transactional(readOnly = true)
    override fun downloadAdFile(filePath: String): ResponseEntity<Any> {
        try {
            val file = File(filePath)
            
            if (!file.exists() || !file.isFile) {
                return ResponseEntity.notFound().build()
            }

            val resource: Resource = FileSystemResource(file)
            val filename = file.name
            val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                .replace("+", "%20")

            val headers = HttpHeaders().apply {
                add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''$encodedFilename")
                contentType = MediaType.APPLICATION_OCTET_STREAM
            }

            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .body(resource)

        } catch (e: Exception) {
            logger.error("파일 다운로드 중 오류 발생: $filePath", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    private fun generateRoundNumber(): String {
        val query = entityManager.createNativeQuery("SELECT generate_round_number()")
        return query.singleResult as String
    }
}