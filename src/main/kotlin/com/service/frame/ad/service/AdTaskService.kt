package com.service.frame.ad.service

import com.service.frame.ad.controller.CreateTestAdTaskRequest
import com.service.frame.ad.dto.AdTaskResponse
import com.service.frame.ad.dto.RoundAdsResponse
import com.service.frame.ad.dto.RoundWithAdsResponse
import com.service.frame.ad.entity.AdTask
import com.service.frame.ad.entity.AdTaskStatus
import com.service.frame.ad.repository.AdTaskRepository
import com.service.frame.round.repository.RoundRepository
import com.service.frame.member.repository.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class AdTaskService(
    private val adTaskRepository: AdTaskRepository,
    private val roundRepository: RoundRepository,
    private val memberRepository: MemberRepository
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

    fun getMemberAdInRound(roundId: Long, memberId: Long): RoundWithAdsResponse {
        val round = roundRepository.findById(roundId).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")
        
        val tasks = adTaskRepository.findByRoundIdAndMemberId(roundId, memberId)
        
        // 현재 참여자 수 계산 (해당 라운드에서 광고 작업이 있는 고유 회원 수)
        val currentParticipants = adTaskRepository.findByRound(round)
            .map { it.member.id }
            .distinct()
            .size
        
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
        
        // 각 멤버에 대해 3개의 광고 타입으로 생성
        request.memberIds.forEach { memberId ->
            val member = memberRepository.findById(memberId).orElse(null)
                ?: throw IllegalArgumentException("Member not found with id: $memberId")

            val adTypes = listOf("scratch", "carousel", "interactive")
            
            adTypes.forEachIndexed { index, adType ->
                val adIndex = index + 1
                val adTask = AdTask(
                    round = round,
                    member = member,
                    status = status,
                    adContent = request.adContent,
                    htmlFilePath = "generated_ads/round_${request.roundId}/member_${memberId}_${adType}_${adIndex}.html",
                    webUrl = "/round_${request.roundId}/member_${memberId}_${adType}_${adIndex}.html",
                    adType = adType,
                    adIndex = adIndex,
                    createdAt = now,
                    updatedAt = now,
                    startedAt = if (status != AdTaskStatus.PENDING) now else null,
                    completedAt = if (status == AdTaskStatus.COMPLETED) now else null
                )
                
                allAdTasks.add(adTaskRepository.save(adTask))
            }
        }

        return allAdTasks
    }
}