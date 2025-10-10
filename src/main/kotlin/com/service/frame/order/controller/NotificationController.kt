package com.service.frame.order.controller

import com.service.frame.order.service.NotificationService
import com.service.frame.order.service.NotificationDto
import com.service.frame.order.service.NotificationHistoryResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/orders/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    /**
     * SSE 연결 생성 (실시간 알림)
     */
    @GetMapping("/sse", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribeToNotifications(@RequestParam memberId: Long): SseEmitter {
        return notificationService.createSseConnection(memberId)
    }

    /**
     * 알림 히스토리 조회
     */
    @GetMapping("/history")
    fun getNotificationHistory(
        @RequestParam memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) type: String?
    ): ResponseEntity<NotificationHistoryResponse> {
        val result = notificationService.getNotificationHistory(memberId, page, size, type)
        return ResponseEntity.ok(result)
    }

    /**
     * 알림 읽음 처리
     */
    @PutMapping("/{notificationId}/read")
    fun markAsRead(
        @RequestParam memberId: Long,
        @PathVariable notificationId: Long
    ): ResponseEntity<NotificationDto> {
        val result = notificationService.markAsRead(memberId, notificationId)
        return ResponseEntity.ok(result)
    }
}