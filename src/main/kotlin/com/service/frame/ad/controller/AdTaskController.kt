package com.service.frame.ad.controller

import com.service.frame.ad.dto.RoundAdsResponse
import com.service.frame.ad.dto.AdTaskResponse
import com.service.frame.ad.dto.RoundWithAdsResponse
import com.service.frame.ad.dto.AdPreviewResponse
import com.service.frame.ad.entity.AdTaskStatus
import com.service.frame.ad.service.AdQueueService
import com.service.frame.ad.service.AdTaskService
import com.service.frame.round.repository.RoundRepository
import com.service.frame.member.repository.MemberRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ad-tasks")
class AdTaskController(
    private val adQueueService: AdQueueService,
    private val adTaskService: AdTaskService,
    private val roundRepository: RoundRepository,
    private val memberRepository: MemberRepository
) {

    /**
     * 라운드별 모든 광고 조회 (상태별 통계 포함)
     */
    @GetMapping("/rounds/{roundId}")
    fun getRoundAds(@PathVariable roundId: Long): ResponseEntity<RoundAdsResponse> {
        val result = adTaskService.getRoundAds(roundId)
        return ResponseEntity.ok(result)
    }


    /**
     * 특정 광고 작업 상세 조회
     */
    @GetMapping("/{taskId}")
    fun getAdTask(@PathVariable taskId: Long): ResponseEntity<AdTaskResponse> {
        val result = adTaskService.getAdTaskById(taskId)
        return ResponseEntity.ok(result)
    }

    /**
     * 특정 회원의 모든 광고 조회
     */
    @GetMapping("/members/{memberId}")
    fun getMemberAds(@PathVariable memberId: Long): ResponseEntity<List<AdTaskResponse>> {
        val result = adTaskService.getMemberAds(memberId)
        return ResponseEntity.ok(result)
    }

    /**
     * 특정 회원의 완료된 광고만 조회
     */
    @GetMapping("/members/{memberId}/completed")
    fun getMemberCompletedAds(@PathVariable memberId: Long): ResponseEntity<List<AdTaskResponse>> {
        val result = adTaskService.getMemberCompletedAds(memberId)
        return ResponseEntity.ok(result)
    }

    /**
     * 특정 회원의 특정 광고 작업 조회
     */
    @GetMapping("/members/{memberId}/tasks/{adTaskId}")
    fun getMemberAdTask(
        @PathVariable memberId: Long,
        @PathVariable adTaskId: Long
    ): ResponseEntity<Any> {
        return try {
            val result = adTaskService.getMemberAdTask(memberId, adTaskId)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            when {
                e.message?.contains("not found") == true -> 
                    ResponseEntity.notFound().build()
                e.message?.contains("does not belong") == true -> 
                    ResponseEntity.status(403).body(mapOf("error" to "Access denied: ${e.message}"))
                else -> 
                    ResponseEntity.badRequest().body(mapOf("error" to e.message))
            }
        }
    }

    /**
     * 특정 회원의 광고 미리보기 데이터 조회
     */
    @GetMapping("/members/{memberId}/previews")
    fun getMemberAdPreviews(@PathVariable memberId: Long): ResponseEntity<AdPreviewResponse> {
        val result = adTaskService.getMemberAdPreviews(memberId)
        return ResponseEntity.ok(result)
    }

    /**
     * 특정 사용자의 특정 광고 미리보기 조회 (권한 확인 포함)
     */
    @GetMapping("/members/{memberId}/ads/{adId}/preview")
    fun getAdTaskPreviewByMember(
        @PathVariable memberId: Long,
        @PathVariable adId: Long
    ): ResponseEntity<Any> {
        return try {
            // 해당 사용자의 광고인지 확인
            val adTask = adTaskService.getMemberAdTask(memberId, adId)
            
            // 회원 정보 조회
            val member = memberRepository.findById(memberId).orElse(null)
                ?: throw IllegalArgumentException("Member not found with id: $memberId")
            
            val previewData = mapOf(
                "id" to adTask.id,
                "title" to "${adTask.roundTitle} - 광고",
                "description" to "라운드: ${adTask.roundTitle}",
                "status" to when (adTask.status) {
                    AdTaskStatus.PENDING -> "대기중"
                    AdTaskStatus.PROCESSING -> "처리중"
                    AdTaskStatus.COMPLETED -> "완료"
                    AdTaskStatus.FAILED -> "실패"
                    AdTaskStatus.RETRY -> "재시도"
                },
                "tags" to listOf("광고", adTask.memberCompanyName, adTask.roundTitle),
                "client" to adTask.memberCompanyName,
                "publisher" to adTask.memberCompanyName,
                "category" to "마케팅 광고",
                "htmlPath" to adTask.webUrl,
                "previewHeight" to "600px",
                "webUrl" to adTask.webUrl,
                "price" to 0,
                "createdAt" to adTask.createdAt,
                "roundId" to adTask.roundId,
                "member" to mapOf(
                    "id" to member.id,
                    "email" to member.email,
                    "companyName" to member.companyName,
                    "businessRegistrationNumber" to member.businessRegistrationNumber,
                    "contactNumber" to member.contactNumber,
                    "businessField" to member.businessField,
                    "productDescription" to member.productDescription,
                    "companyDescription" to member.companyDescription,
                    "isPremium" to member.isPremium,
                    "rentalStatus" to member.rentalStatus.name,
                    "currentRentalExpiry" to member.currentRentalExpiry
                )
            )
            ResponseEntity.ok(previewData)
        } catch (e: IllegalArgumentException) {
            when {
                e.message?.contains("not found") == true -> 
                    ResponseEntity.notFound().build()
                e.message?.contains("does not belong") == true -> 
                    ResponseEntity.status(403).body(mapOf("error" to "해당 광고에 대한 접근 권한이 없습니다."))
                else -> 
                    ResponseEntity.badRequest().body(mapOf("error" to e.message))
            }
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
        }
    }

    /**
     * 공개 광고 미리보기 조회 (완료된 광고만)
     */
    @GetMapping("/{adId}/preview")
    fun getPublicAdTaskPreview(@PathVariable adId: Long): ResponseEntity<Any> {
        return try {
            val adTask = adTaskService.getAdTaskById(adId)
            
            // 완료된 광고만 공개 조회 가능
            if (adTask.status != AdTaskStatus.COMPLETED) {
                return ResponseEntity.status(403).body(mapOf("error" to "완료되지 않은 광고는 조회할 수 없습니다."))
            }
            
            val previewData = mapOf(
                "id" to adTask.id,
                "title" to "${adTask.roundTitle} 광고",
                "description" to "라운드: ${adTask.roundTitle}",
                "status" to "완료",
                "tags" to listOf("광고", adTask.roundTitle),
                "client" to "***", // 개인정보 보호
                "publisher" to "***", // 개인정보 보호  
                "category" to "마케팅 광고",
                "htmlPath" to adTask.webUrl,
                "previewHeight" to "600px",
                "webUrl" to adTask.webUrl,
                "price" to 0,
                "createdAt" to adTask.createdAt
            )
            ResponseEntity.ok(previewData)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to "서버 오류가 발생했습니다."))
        }
    }

    /**
     * 랜덤 광고 미리보기 조회 (현재 회원이 제작한 광고 제외)
     */
    @GetMapping("/previews/random")
    fun getRandomAdPreviews(
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(required = false) excludeMemberId: Long?
    ): ResponseEntity<AdPreviewResponse> {
        val result = adTaskService.getRandomAdPreviews(limit, excludeMemberId)
        return ResponseEntity.ok(result)
    }

    /**
     * 특정 라운드에서 특정 회원의 완료된 광고 조회 (라운드 정보 포함)
     */
    @GetMapping("/rounds/{roundId}/members/{memberId}")
    fun getMemberAdInRound(
        @PathVariable roundId: Long,
        @PathVariable memberId: Long
    ): ResponseEntity<RoundWithAdsResponse> {
        val result = adTaskService.getMemberAdInRound(roundId, memberId)
        return ResponseEntity.ok(result)
    }

    /**
     * 라운드별 광고 생성 작업 통계 조회
     */
    @GetMapping("/rounds/{roundId}/statistics")
    fun getRoundAdTaskStatistics(@PathVariable roundId: Long): ResponseEntity<Map<String, Any>> {
        val round = roundRepository.findById(roundId).orElse(null)
            ?: return ResponseEntity.notFound().build()

        val statistics = adQueueService.getRoundTaskStatistics(round)
        
        val result = mapOf(
            "roundId" to roundId,
            "roundTitle" to round.title,
            "statistics" to statistics.mapKeys { it.key.name },
            "totalTasks" to statistics.values.sum()
        )

        return ResponseEntity.ok(result)
    }

    /**
     * 특정 작업 상태 업데이트 (Python 워커에서 호출)
     */
    @PutMapping("/{taskId}/status")
    fun updateTaskStatus(
        @PathVariable taskId: Long,
        @RequestBody request: TaskStatusUpdateRequest
    ): ResponseEntity<Map<String, String>> {
        
        val status = try {
            AdTaskStatus.valueOf(request.status.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(
                mapOf("error" to "Invalid status: ${request.status}")
            )
        }

        adQueueService.updateTaskStatus(
            taskId = taskId,
            status = status,
            htmlFilePath = request.htmlFilePath,
            errorMessage = request.errorMessage
        )

        return ResponseEntity.ok(mapOf("message" to "Task status updated successfully"))
    }

    /**
     * 실패한 작업 재시도
     */
    @PostMapping("/{taskId}/retry")
    fun retryFailedTask(
        @PathVariable taskId: Long,
        @RequestBody request: RetryTaskRequest
    ): ResponseEntity<Map<String, String>> {
        
        adQueueService.requeueFailedTask(taskId, request.errorMessage ?: "Manual retry")
        
        return ResponseEntity.ok(mapOf("message" to "Task queued for retry"))
    }

    /**
     * 수동으로 라운드의 모든 PENDING 작업을 큐에 추가
     */
    @PostMapping("/rounds/{roundId}/enqueue-pending")
    fun enqueuePendingTasks(@PathVariable roundId: Long): ResponseEntity<Map<String, Any>> {
        val round = roundRepository.findById(roundId).orElse(null)
            ?: return ResponseEntity.notFound().build()

        try {
            val result = adQueueService.manualEnqueuePendingTasks(round)
            return ResponseEntity.ok(mapOf(
                "message" to "Pending tasks enqueued successfully",
                "enqueuedCount" to result
            ))
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(mapOf(
                "error" to "Failed to enqueue tasks: ${e.message}"
            ))
        }
    }

    /**
     * 기존 라운드에서 광고를 만들지 않은 신규 회원들을 위한 광고 생성
     */
    @PostMapping("/rounds/{roundId}/create-missing-ads")
    fun createMissingAdsForRound(@PathVariable roundId: Long): ResponseEntity<Map<String, Any>> {
        val round = roundRepository.findById(roundId).orElse(null)
            ?: return ResponseEntity.notFound().build()

        try {
            val result = adTaskService.createMissingAdsForNewMembers(roundId)
            
            // 현재 참여자 수 다시 확인 (결과 반영)
            val existingTasks = adTaskService.getRoundAds(roundId)
            val currentParticipants = existingTasks.ads.map { it.memberId }.toSet().size
            
            val responseMessage = if (result.isEmpty()) {
                if (round.maxParticipants != null && currentParticipants >= round.maxParticipants) {
                    "라운드가 최대 참여자 수에 도달하여 처리하지 않았습니다"
                } else {
                    "모든 활성 회원이 이미 이 라운드에 참여하고 있습니다"
                }
            } else {
                "Missing ads created successfully"
            }
            
            return ResponseEntity.ok(mapOf<String, Any>(
                "message" to responseMessage,
                "roundId" to roundId,
                "roundTitle" to round.title,
                "maxParticipants" to (round.maxParticipants ?: "unlimited"),
                "currentParticipants" to currentParticipants,
                "availableSlots" to if (round.maxParticipants != null) (round.maxParticipants - currentParticipants) else "unlimited",
                "newMembersCount" to result.size,
                "totalAdTasksCreated" to result.sumOf { it.second.size },
                "memberTasks" to result.map { (member, adTasks) ->
                    mapOf(
                        "memberId" to member.id,
                        "memberEmail" to member.email,
                        "companyName" to member.companyName,
                        "adTaskIds" to adTasks.map { it.id },
                        "adTypes" to adTasks.map { "${it.adType}_${it.adIndex}" }
                    )
                }
            ))
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(mapOf(
                "error" to "Failed to create missing ads: ${e.message}"
            ))
        }
    }

    /**
     * 테스트용 ad_task 생성 API (여러 멤버에 대해 각각 3개의 광고를 자동 생성)
     */
    @PostMapping("/test-data")
    fun createTestAdTask(@RequestBody request: CreateTestAdTaskRequest): ResponseEntity<Map<String, Any>> {
        return try {
            val allAdTasks = adTaskService.createTestAdTasksForMembers(request)
            val memberTasksMap = allAdTasks.groupBy { it.member.id }
            
            ResponseEntity.ok(mapOf<String, Any>(
                "message" to "Test ad tasks created successfully for ${request.memberIds.size} members",
                "totalCount" to allAdTasks.size,
                "membersCount" to request.memberIds.size,
                "roundId" to (allAdTasks.firstOrNull()?.round?.id ?: 0L),
                "memberTasks" to memberTasksMap.map { (memberId, tasks) ->
                    mapOf(
                        "memberId" to memberId,
                        "adTaskIds" to tasks.map { it.id ?: 0L },
                        "adTypes" to tasks.map { "${it.adType}_${it.adIndex}" }
                    )
                }
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf<String, Any>(
                "error" to "Failed to create test ad tasks: ${e.message}"
            ))
        }
    }
}

data class TaskStatusUpdateRequest(
    val status: String,
    val htmlFilePath: String? = null,
    val errorMessage: String? = null
)

data class RetryTaskRequest(
    val errorMessage: String? = null
)

data class CreateTestAdTaskRequest(
    val roundId: Long,
    val memberIds: List<Long>,
    val taskStatus: String = "COMPLETED",
    val adContent: String? = null,
    val htmlFilePath: String? = null,
    val webUrl: String? = null,
    val adType: String = "interactive",
)