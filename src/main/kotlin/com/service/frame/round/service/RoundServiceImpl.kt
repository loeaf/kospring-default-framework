package com.service.frame.round.service

import com.service.frame.ad.service.AdQueueService
import com.service.frame.member.repository.MemberRepository
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
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Service
@Transactional
class RoundServiceImpl(
    private val roundRepository: RoundRepository,
    private val memberRepository: MemberRepository,
    private val adQueueService: AdQueueService,
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
        
        return RoundResponse.from(savedRound)
    }

    @Transactional(readOnly = true)
    override fun getRounds(status: RoundStatus?, pageable: Pageable): Page<RoundResponse> {
        val rounds = if (status != null) {
            roundRepository.findActiveRoundsOrderByCreatedAtDesc(status, pageable)
        } else {
            roundRepository.findAll(pageable)
        }
        
        return rounds.map { RoundResponse.from(it) }
    }

    @Transactional(readOnly = true)
    override fun getRoundById(id: Long): RoundResponse {
        val round = roundRepository.findById(id).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $id")
        
        return RoundResponse.from(round)
    }

    @Transactional(readOnly = true)
    override fun getRoundsByCreatedBy(memberId: Long, pageable: Pageable): Page<RoundResponse> {
        val rounds = roundRepository.findByCreatedByIdOrderByCreatedAtDesc(memberId, pageable)
        return rounds.map { RoundResponse.from(it) }
    }

    private fun generateRoundNumber(): String {
        val query = entityManager.createNativeQuery("SELECT generate_round_number()")
        return query.singleResult as String
    }
}