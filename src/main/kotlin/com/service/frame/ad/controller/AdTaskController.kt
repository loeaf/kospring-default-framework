package com.service.frame.ad.controller

import com.service.frame.ad.dto.RoundAdsResponse
import com.service.frame.ad.dto.AdTaskResponse
import com.service.frame.ad.dto.RoundWithAdsResponse
import com.service.frame.ad.dto.AdPreviewResponse
import com.service.frame.ad.entity.AdTaskStatus
import com.service.frame.ad.service.AdQueueService
import com.service.frame.ad.service.AdTaskService
import com.service.frame.round.repository.RoundRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ad-tasks")
class AdTaskController(
    private val adQueueService: AdQueueService,
    private val adTaskService: AdTaskService,
    private val roundRepository: RoundRepository
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
     * 특정 회원의 광고 미리보기 데이터 조회
     */
    @GetMapping("/members/{memberId}/previews")
    fun getMemberAdPreviews(@PathVariable memberId: Long): ResponseEntity<AdPreviewResponse> {
        val result = adTaskService.getMemberAdPreviews(memberId)
        return ResponseEntity.ok(result)
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