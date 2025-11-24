package com.service.frame.ad.service

import com.service.frame.ad.controller.CreateTestAdTaskRequest
import com.service.frame.ad.dto.AdTaskResponse
import com.service.frame.ad.dto.RoundAdsResponse
import com.service.frame.ad.dto.RoundWithAdsResponse
import com.service.frame.ad.dto.AdPreviewResponse
import com.service.frame.ad.dto.AdPreviewItem
import com.service.frame.ad.dto.AdPreviewStatistics
import com.service.frame.ad.dto.MultiLanguageText
import com.service.frame.ad.entity.AdTask
import com.service.frame.ad.entity.AdTaskStatus
import com.service.frame.ad.repository.AdTaskRepository
import com.service.frame.round.repository.RoundRepository
import com.service.frame.member.repository.MemberRepository
import com.service.frame.order.repository.OrderRepository
import com.service.frame.post.repository.AdvertisementAssignmentRepository
import com.service.frame.post.repository.AdvertisementPostRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class AdTaskService(
    private val adTaskRepository: AdTaskRepository,
    private val roundRepository: RoundRepository,
    private val memberRepository: MemberRepository,
    private val orderRepository: OrderRepository,
    private val assignmentRepository: AdvertisementAssignmentRepository,
    private val advertisementPostRepository: AdvertisementPostRepository,
    private val adProperties: com.service.frame.config.AdProperties
) {
    private val logger = LoggerFactory.getLogger(AdTaskService::class.java)

    fun getRoundAds(roundId: Long): RoundAdsResponse {
        val round = roundRepository.findById(roundId).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")

        val allTasks = adTaskRepository.findByRound(round)
        
        val totalAds = allTasks.size
        val completedAds = allTasks.count { it.status == AdTaskStatus.COMPLETED }
        val pendingAds = allTasks.count { it.status == AdTaskStatus.PENDING || it.status == AdTaskStatus.PROCESSING }
        val failedAds = allTasks.count { it.status == AdTaskStatus.FAILED }

        val adResponses = allTasks.map { task ->
            AdTaskResponse(
                id = task.id!!,
                roundId = task.round.id!!,
                roundTitle = task.round.title,
                memberId = task.member.id!!,
                memberCompanyName = task.member.companyName ?: "",
                memberEmail = task.member.email,
                status = task.status,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt,
                completedAt = task.completedAt,
                errorMessage = task.errorMessage,
                retryCount = task.retryCount,
                webUrl = task.webUrl
            )
        }

        return RoundAdsResponse(
            roundId = roundId,
            roundTitle = round.title,
            totalAds = totalAds,
            completedAds = completedAds,
            pendingAds = pendingAds,
            failedAds = failedAds,
            ads = adResponses
        )
    }


    fun getAdTaskById(taskId: Long): AdTaskResponse {
        val task = adTaskRepository.findById(taskId).orElse(null)
            ?: throw IllegalArgumentException("AdTask not found with id: $taskId")

        return AdTaskResponse(
            id = task.id!!,
            roundId = task.round.id!!,
            roundTitle = task.round.title,
            memberId = task.member.id!!,
            memberCompanyName = task.member.companyName ?: "",
            memberEmail = task.member.email,
            status = task.status,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
            completedAt = task.completedAt,
            errorMessage = task.errorMessage,
            retryCount = task.retryCount,
            webUrl = task.webUrl
        )
    }

    fun getMemberAds(memberId: Long): List<AdTaskResponse> {
        val memberTasks = adTaskRepository.findByMemberId(memberId)
        
        return memberTasks.map { task ->
            AdTaskResponse(
                id = task.id!!,
                roundId = task.round.id!!,
                roundTitle = task.round.title,
                memberId = task.member.id!!,
                memberCompanyName = task.member.companyName ?: "",
                memberEmail = task.member.email,
                status = task.status,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt,
                completedAt = task.completedAt,
                errorMessage = task.errorMessage,
                retryCount = task.retryCount,
                webUrl = task.webUrl
            )
        }
    }

    fun getMemberCompletedAds(memberId: Long): List<AdTaskResponse> {
        val completedTasks = adTaskRepository.findCompletedAdsByMemberId(memberId)
        
        return completedTasks.map { task ->
            AdTaskResponse(
                id = task.id!!,
                roundId = task.round.id!!,
                roundTitle = task.round.title,
                memberId = task.member.id!!,
                memberCompanyName = task.member.companyName ?: "",
                memberEmail = task.member.email,
                status = task.status,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt,
                completedAt = task.completedAt,
                errorMessage = task.errorMessage,
                retryCount = task.retryCount,
                webUrl = task.webUrl
            )
        }
    }

    fun getMemberAdTask(memberId: Long, adTaskId: Long): AdTaskResponse {
        val task = adTaskRepository.findById(adTaskId).orElse(null)
            ?: throw IllegalArgumentException("AdTask not found with id: $adTaskId")
        
        if (task.member.id != memberId) {
            throw IllegalArgumentException("AdTask $adTaskId does not belong to member $memberId")
        }

        return AdTaskResponse(
            id = task.id!!,
            roundId = task.round.id!!,
            roundTitle = task.round.title,
            memberId = task.member.id!!,
            memberCompanyName = task.member.companyName ?: "",
            memberEmail = task.member.email,
            status = task.status,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
            completedAt = task.completedAt,
            errorMessage = task.errorMessage,
            retryCount = task.retryCount,
            webUrl = task.webUrl
        )
    }

    fun getMemberAdInRound(roundId: Long, memberId: Long): RoundWithAdsResponse {
        val round = roundRepository.findById(roundId).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")
        
        val tasks = adTaskRepository.findByRoundIdAndMemberId(roundId, memberId)
        
        // 현재 참여자 수 계산 (해당 라운드의 주문 수)
        val currentParticipants = orderRepository.countByRoundId(roundId).toInt()
        
        // 남은 시간 계산 (밀리초)
        val now = LocalDateTime.now()
        val remainingTimeMs = if (round.endDate.isAfter(now)) {
            java.time.Duration.between(now, round.endDate).toMillis()
        } else {
            0L
        }
        
        val adResponses = tasks.map { task ->
            AdTaskResponse(
                id = task.id!!,
                roundId = task.round.id!!,
                roundTitle = task.round.title,
                memberId = task.member.id!!,
                memberCompanyName = task.member.companyName ?: "",
                memberEmail = task.member.email,
                status = task.status,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt,
                completedAt = task.completedAt,
                errorMessage = task.errorMessage,
                retryCount = task.retryCount,
                webUrl = task.webUrl
            )
        }
        
        return RoundWithAdsResponse(
            roundId = round.id!!,
            roundTitle = round.title,
            roundDescription = round.description,
            category = round.category,
            orderAmount = round.orderAmount,
            templateCost = round.templateCost,
            aiGenerationCost = round.aiGenerationCost,
            targetingPostingCost = round.targetingPostingCost,
            serverRentalCost = round.serverRentalCost,
            otherCosts = round.otherCosts,
            startDate = round.startDate,
            endDate = round.endDate,
            postStartDate = round.getCalculatedPostStartDate(),
            postEndDate = round.getCalculatedPostEndDate(),
            status = round.status.name,
            maxParticipants = round.maxParticipants,
            currentParticipants = currentParticipants,
            remainingTimeMs = remainingTimeMs,
            ads = adResponses
        )
    }

    @Transactional
    fun createTestAdTasksForMembers(request: CreateTestAdTaskRequest): List<AdTask> {
        val round = roundRepository.findById(request.roundId).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: ${request.roundId}")

        val status = try {
            AdTaskStatus.valueOf(request.taskStatus.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid task status: ${request.taskStatus}")
        }

        val now = LocalDateTime.now()
        val allAdTasks = mutableListOf<AdTask>()
        
        // 기존 AdTask 조회 (중복 방지)
        val existingTasks = adTaskRepository.findByRound(round)
        val existingKeys = existingTasks.map { "${request.roundId}_${it.member.id}_${it.adType}_${it.adIndex}" }.toSet()
        
        // 각 멤버에 대해 3개의 광고 타입으로 생성
        request.memberIds.forEach { memberId ->
            val member = memberRepository.findById(memberId).orElse(null)
                ?: throw IllegalArgumentException("Member not found with id: $memberId")

            // 설정된 개수만큼 광고 생성
            for (adIndex in 1..adProperties.count) {
                val adType = "ad_type_$adIndex"
                val key = "${request.roundId}_${memberId}_${adType}_${adIndex}"
                
                // 이미 존재하는 조합은 건너뛰기
                if (existingKeys.contains(key)) {
                    logger.info("AdTask already exists for round ${request.roundId}, member $memberId, type $adType, index $adIndex - skipping")
                    
                    // 기존 태스크를 결과에 포함 (상태 업데이트 가능)
                    val existingTask = existingTasks.find { 
                        it.member.id == memberId && it.adType == adType && it.adIndex == adIndex 
                    }
                    if (existingTask != null) {
                        val updatedTask = existingTask.copy(
                            status = status,
                            adContent = request.adContent,
                            updatedAt = now,
                            startedAt = if (status != AdTaskStatus.PENDING && existingTask.startedAt == null) now else existingTask.startedAt,
                            completedAt = if (status == AdTaskStatus.COMPLETED && existingTask.completedAt == null) now else existingTask.completedAt
                        )
                        allAdTasks.add(adTaskRepository.save(updatedTask))
                    }
                    continue
                }
                
                val timestamp = System.currentTimeMillis() / 1000
                val filename = "member_${memberId}_${adType}_${timestamp}.html"
                val htmlFilePath = "generated_ads/round_${request.roundId}/${filename}"
                val webUrl = "/round_${request.roundId}/${filename}"
                
                val adTask = AdTask(
                    round = round,
                    member = member,
                    status = status,
                    adContent = request.adContent,
                    htmlFilePath = htmlFilePath,
                    webUrl = webUrl,
                    adType = adType,
                    adIndex = adIndex,
                    createdAt = now,
                    updatedAt = now,
                    startedAt = if (status != AdTaskStatus.PENDING) now else null,
                    completedAt = if (status == AdTaskStatus.COMPLETED) now else null
                )
                
                allAdTasks.add(adTaskRepository.save(adTask))
                logger.info("Created new AdTask for round ${request.roundId}, member $memberId, type $adType, index $adIndex")
            }
        }

        logger.info("Created or updated ${allAdTasks.size} AdTasks for round ${request.roundId}")
        return allAdTasks
    }

    fun getMemberAdPreviews(memberId: Long): AdPreviewResponse {
        val assignments = assignmentRepository.findByPublisherMemberIdOrderByCreatedAtDesc(memberId)
        
        val adPreviewItems = assignments.mapNotNull { assignment ->
            val task = assignment.adTask ?: return@mapNotNull null
            val round = assignment.round ?: return@mapNotNull null
            val advertiser = assignment.advertiserMember ?: return@mapNotNull null
            val publisher = assignment.publisherMember ?: return@mapNotNull null
            
            // 실제 게시된 광고 포스트만 조회 (없으면 제외)
            val advertisementPost = advertisementPostRepository.findByAssignmentId(assignment.id!!) ?: return@mapNotNull null
            
            AdPreviewItem(
                id = "ad_${task.id}",
                title = MultiLanguageText(
                    ko = "${round.title}",
                    en = ""
                ),
                description = MultiLanguageText(
                    ko = task.description ?: "",
                    en = ""
                ),
                client = MultiLanguageText(
                    ko = advertiser.companyName,
                    en = ""
                ),
                publisher = MultiLanguageText(
                    ko = publisher.companyName,
                    en = ""
                ),
                tags = listOfNotNull(task.adType, round.category ?: "general"),
                htmlPath = task.webUrl ?: "",
                previewHeight = "600px",
                category = round.category ?: "general",
                status = advertisementPost.postStatus.name,
                createdAt = advertisementPost.createdAt.toString()
            )
        }
        
        val categories = assignments.mapNotNull { it.round?.category }.distinct()
        
        val statistics = AdPreviewStatistics(
            totalAds = adPreviewItems.size,
            featuredAds = adPreviewItems.size,
            categories = categories
        )
        
        return AdPreviewResponse(
            featured = adPreviewItems,
            statistics = statistics
        )
    }

    fun getRandomAdPreviews(limit: Int, excludeMemberId: Long?): AdPreviewResponse {
        val allAssignments = if (excludeMemberId != null) {
            // 특정 회원이 제작한 광고를 제외
            assignmentRepository.findAll().filter { it.publisherMember?.id != excludeMemberId }
        } else {
            // 모든 광고
            assignmentRepository.findAll()
        }
        
        // 완료된 광고만 필터링 (옵션)
        val completedAssignments = allAssignments.filter { assignment ->
            assignment.adTask?.status == AdTaskStatus.COMPLETED
        }
        
        // 랜덤하게 섞어서 limit 개수만큼 선택
        val randomAssignments = if (completedAssignments.size > limit) {
            completedAssignments.shuffled().take(limit)
        } else {
            completedAssignments.shuffled()
        }
        
        val adPreviewItems = randomAssignments.mapNotNull { assignment ->
            val task = assignment.adTask ?: return@mapNotNull null
            val round = assignment.round ?: return@mapNotNull null
            val advertiser = assignment.advertiserMember ?: return@mapNotNull null
            val publisher = assignment.publisherMember ?: return@mapNotNull null
            
            // 실제 게시된 광고 포스트만 조회 (없으면 제외)
            val advertisementPost = advertisementPostRepository.findByAssignmentId(assignment.id!!) ?: return@mapNotNull null
            
            AdPreviewItem(
                id = "ad_${task.id}",
                title = MultiLanguageText(
                    ko = "${round.title}",
                    en = ""
                ),
                description = MultiLanguageText(
                    ko = task.description ?: "",
                    en = ""
                ),
                client = MultiLanguageText(
                    ko = advertiser.companyName,
                    en = ""
                ),
                publisher = MultiLanguageText(
                    ko = publisher.companyName,
                    en = ""
                ),
                tags = listOfNotNull(task.adType, round.category ?: "general"),
                htmlPath = task.webUrl ?: "",
                previewHeight = "600px",
                category = round.category ?: "general",
                status = advertisementPost.postStatus.name,
                createdAt = advertisementPost.createdAt.toString()
            )
        }
        
        val categories = randomAssignments.mapNotNull { it.round?.category }.distinct()
        
        val statistics = AdPreviewStatistics(
            totalAds = adPreviewItems.size,
            featuredAds = adPreviewItems.size,
            categories = categories
        )
        
        return AdPreviewResponse(
            featured = adPreviewItems,
            statistics = statistics
        )
    }

    /**
     * 기존 라운드에서 광고를 만들지 않은 신규 회원들을 위한 광고 생성
     */
    @Transactional
    fun createMissingAdsForNewMembers(roundId: Long): List<Pair<com.service.frame.member.entity.Member, List<AdTask>>> {
        val round = roundRepository.findById(roundId).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")

        // 해당 라운드에서 이미 광고를 만든 회원들 조회
        val existingTasks = adTaskRepository.findByRound(round)
        val existingMemberIds = existingTasks.map { it.member.id }.toSet()
        val currentParticipants = existingMemberIds.size

        // 최대 참여자 수 제한 확인 - 이미 꽉 찬 경우 처리하지 않음
        val maxParticipants = round.maxParticipants
        if (maxParticipants != null && currentParticipants >= maxParticipants) {
            logger.info("Round $roundId has reached maximum participants ($currentParticipants/$maxParticipants) - no processing needed")
            return emptyList()
        }

        // 유효한 임대권을 가진 모든 활성 회원 조회
        val allActiveMembers = memberRepository.findActiveMembers()

        // 3개 광고를 모두 만들지 않은 회원들 필터링 (ad_index 기준)
        val newMembers = allActiveMembers.filter { member ->
            val memberTasks = existingTasks.filter { it.member.id == member.id }
            val existingIndexes = memberTasks.mapNotNull { it.adIndex }.toSet()
            val requiredIndexes = (1..adProperties.count).toSet()
            
            // 필요한 인덱스가 모두 없는 경우에만 신규 회원으로 간주
            !requiredIndexes.all { existingIndexes.contains(it) }
        }

        if (newMembers.isEmpty()) {
            logger.info("No new members found for round $roundId - all active members already have ads")
            return emptyList()
        }

        val now = LocalDateTime.now()
        val result = mutableListOf<Pair<com.service.frame.member.entity.Member, List<AdTask>>>()

        newMembers.forEach { member ->
            val memberAdTasks = mutableListOf<AdTask>()
            
            // 해당 회원의 기존 광고 인덱스 확인
            val memberTasks = existingTasks.filter { it.member.id == member.id }
            val existingIndexes = memberTasks.mapNotNull { it.adIndex }.toSet()
            
            // 설정된 광고 개수만큼 인덱스별로 확인하여 누락된 것만 생성
            for (adIndex in 1..adProperties.count) {
                // 이미 존재하는 인덱스는 건너뛰기
                if (existingIndexes.contains(adIndex)) {
                    logger.info("AdTask already exists for round $roundId, member ${member.id}, index $adIndex - skipping")
                    continue
                }
                
                val timestamp = System.currentTimeMillis() / 1000
                val filename = "member_${member.id}_index${adIndex}_${timestamp}.html"
                val htmlFilePath = "generated_ads/round_${roundId}/${filename}"
                val webUrl = "/round_${roundId}/${filename}"
                
                try {
                    val adTask = AdTask(
                        round = round,
                        member = member,
                        status = AdTaskStatus.PENDING,
                        adContent = "신규 회원을 위한 자동 생성된 광고 - Index $adIndex",
                        htmlFilePath = htmlFilePath,
                        webUrl = webUrl,
                        adType = "auto_generated", // 타입은 자동 생성으로 통일
                        adIndex = adIndex,
                        createdAt = now,
                        updatedAt = now,
                        startedAt = null,
                        completedAt = null
                    )
                    
                    val savedAdTask = adTaskRepository.save(adTask)
                    memberAdTasks.add(savedAdTask)
                    
                    logger.info("Created missing AdTask for round $roundId, member ${member.id} (${member.companyName}), index $adIndex")
                } catch (e: Exception) {
                    logger.warn("Failed to create AdTask for round $roundId, member ${member.id}, index $adIndex - likely duplicate: ${e.message}")
                }
            }
            
            if (memberAdTasks.isNotEmpty()) {
                result.add(Pair(member, memberAdTasks))
            }
        }

        logger.info("Created ${result.sumOf { it.second.size }} missing AdTasks for ${newMembers.size} new members in round $roundId")
        return result
    }
}