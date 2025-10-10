package com.service.frame.order.service

import com.service.frame.order.entity.Order
import com.service.frame.order.entity.OrderStatus
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Service
class NotificationService(
    private val objectMapper: ObjectMapper
) {
    
    private val sseEmitters = ConcurrentHashMap<Long, MutableList<SseEmitter>>()
    private val notificationHistory = ConcurrentHashMap<Long, MutableList<NotificationDto>>()

    fun createSseConnection(memberId: Long): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)
        
        sseEmitters.computeIfAbsent(memberId) { CopyOnWriteArrayList() }.add(emitter)
        
        emitter.onCompletion {
            sseEmitters[memberId]?.remove(emitter)
        }
        
        emitter.onTimeout {
            sseEmitters[memberId]?.remove(emitter)
        }
        
        emitter.onError {
            sseEmitters[memberId]?.remove(emitter)
        }
        
        return emitter
    }

    fun sendOrderStatusNotification(order: Order, oldStatus: OrderStatus, newStatus: OrderStatus) {
        val notification = NotificationDto(
            id = generateId(),
            type = NotificationType.ORDER_STATUS_CHANGED,
            orderId = order.id!!,
            orderNumber = order.orderNumber ?: "",
            title = "주문 상태 변경",
            message = getStatusChangeMessage(oldStatus, newStatus),
            isRead = false,
            createdAt = LocalDateTime.now(),
            data = mapOf(
                "oldStatus" to oldStatus.name,
                "newStatus" to newStatus.name,
                "progressRate" to order.progressRate
            )
        )
        
        sendNotification(order.member.id!!, notification)
    }

    fun sendPaymentConfirmedNotification(order: Order, paymentAmount: java.math.BigDecimal) {
        val notification = NotificationDto(
            id = generateId(),
            type = NotificationType.PAYMENT_CONFIRMED,
            orderId = order.id!!,
            orderNumber = order.orderNumber ?: "",
            title = "결제 확인",
            message = "입금이 확인되었습니다.",
            isRead = false,
            createdAt = LocalDateTime.now(),
            data = mapOf(
                "paymentAmount" to paymentAmount.toString()
            )
        )
        
        sendNotification(order.member.id!!, notification)
    }

    fun sendFilesUploadedNotification(order: Order, fileCount: Int) {
        val notification = NotificationDto(
            id = generateId(),
            type = NotificationType.FILES_UPLOADED,
            orderId = order.id!!,
            orderNumber = order.orderNumber ?: "",
            title = "결과물 업로드",
            message = "${fileCount}개의 결과물이 업로드되었습니다.",
            isRead = false,
            createdAt = LocalDateTime.now(),
            data = mapOf(
                "fileCount" to fileCount
            )
        )
        
        sendNotification(order.member.id!!, notification)
    }

    private fun sendNotification(memberId: Long, notification: NotificationDto) {
        // 히스토리에 저장
        notificationHistory.computeIfAbsent(memberId) { CopyOnWriteArrayList() }.add(notification)
        
        // SSE로 실시간 전송
        val emitters = sseEmitters[memberId] ?: return
        val toRemove = mutableListOf<SseEmitter>()
        
        emitters.forEach { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name(notification.type.eventName)
                        .data(objectMapper.writeValueAsString(notification))
                )
            } catch (e: Exception) {
                toRemove.add(emitter)
            }
        }
        
        // 실패한 emitter 제거
        emitters.removeAll(toRemove)
    }

    fun getNotificationHistory(
        memberId: Long, 
        page: Int = 0, 
        size: Int = 20, 
        type: String? = null
    ): NotificationHistoryResponse {
        val allNotifications = notificationHistory[memberId] ?: emptyList()
        
        val filteredNotifications = if (type != null) {
            allNotifications.filter { it.type.name.equals(type, ignoreCase = true) }
        } else {
            allNotifications
        }
        
        val sortedNotifications = filteredNotifications.sortedByDescending { it.createdAt }
        val totalElements = sortedNotifications.size
        val totalPages = (totalElements + size - 1) / size
        
        val startIndex = page * size
        val endIndex = minOf(startIndex + size, totalElements)
        val content = if (startIndex < totalElements) {
            sortedNotifications.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return NotificationHistoryResponse(
            content = content,
            totalElements = totalElements,
            totalPages = totalPages,
            size = size,
            number = page
        )
    }

    fun markAsRead(memberId: Long, notificationId: Long): NotificationDto {
        val notifications = notificationHistory[memberId] 
            ?: throw IllegalArgumentException("Member not found: $memberId")
        
        val notification = notifications.find { it.id == notificationId }
            ?: throw IllegalArgumentException("Notification not found: $notificationId")
        
        val updatedNotification = notification.copy(
            isRead = true,
            readAt = LocalDateTime.now()
        )
        
        // 리스트에서 기존 알림을 찾아서 교체
        val index = notifications.indexOf(notification)
        if (index >= 0) {
            notifications[index] = updatedNotification
        }
        
        return updatedNotification
    }

    private fun getStatusChangeMessage(oldStatus: OrderStatus, newStatus: OrderStatus): String {
        return when (newStatus) {
            OrderStatus.PAYMENT_WAITING -> "결제 정보가 생성되었습니다."
            OrderStatus.PAYMENT_CONFIRMED -> "입금이 확인되었습니다."
            OrderStatus.APPROVED -> "주문이 승인되었습니다."
            OrderStatus.IN_PROGRESS -> "작업이 시작되었습니다."
            OrderStatus.COMPLETED -> "작업이 완료되었습니다."
            OrderStatus.FAILED -> "작업이 실패했습니다."
            OrderStatus.CANCELLED -> "주문이 취소되었습니다."
            else -> "주문 상태가 변경되었습니다."
        }
    }

    private fun generateId(): Long {
        return System.currentTimeMillis()
    }
}

// DTOs
data class NotificationDto(
    val id: Long,
    val type: NotificationType,
    val orderId: Long,
    val orderNumber: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
    val readAt: LocalDateTime? = null,
    val data: Map<String, Any>? = null
)

enum class NotificationType(val eventName: String) {
    ORDER_STATUS_CHANGED("orderStatusChanged"),
    PAYMENT_CONFIRMED("paymentConfirmed"),
    ORDER_APPROVED("orderApproved"),
    ORDER_STARTED("orderStarted"),
    ORDER_COMPLETED("orderCompleted"),
    ORDER_FAILED("orderFailed"),
    FILES_UPLOADED("filesUploaded")
}

data class NotificationHistoryResponse(
    val content: List<NotificationDto>,
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    val number: Int
)