package com.service.frame.order.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class RoundInfoResponse(
    val roundId: Long,
    val roundTitle: String,
    val adTasks: List<AdTaskInfo>,
    val orders: List<OrderInfo>,
    val payments: List<PaymentInfo>
)

data class AdTaskInfo(
    val id: Long,
    val memberId: Long,
    val memberEmail: String,
    val memberCompanyName: String?,
    val adType: String?,
    val adIndex: Int?,
    val taskStatus: String,
    val webUrl: String?,
    val createdAt: LocalDateTime
)

data class OrderInfo(
    val id: Long,
    val orderNumber: String?,
    val adTaskId: Long,
    val memberId: Long,
    val memberEmail: String,
    val memberCompanyName: String?,
    val productName: String,
    val quantity: Int,
    val status: String,
    val submittedAt: LocalDateTime,
    val createdAt: LocalDateTime
)

data class PaymentInfo(
    val id: Long,
    val orderId: Long,
    val applicationNumber: String,
    val paymentAmount: BigDecimal,
    val depositorName: String?,
    val paymentStatus: String,
    val bankName: String,
    val bankAccountNumber: String,
    val paymentConfirmedAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

// ID만 포함하는 간단한 버전 (멤버별로 그룹핑)
data class RoundKeysResponse(
    val roundId: Long,
    val roundTitle: String,
    val members: List<MemberKeys>
)

data class MemberKeys(
    val memberId: Long,
    val memberEmail: String,
    val memberCompanyName: String?,
    val adTaskId: Long?,
    val orderId: Long?,
    val paymentIds: List<Long>
)