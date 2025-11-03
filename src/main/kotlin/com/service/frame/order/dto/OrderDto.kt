package com.service.frame.order.dto

import com.service.frame.order.entity.OrderStatus
import com.service.frame.order.entity.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

// 주문 생성 요청
data class OrderCreateRequest(
    val adTaskId: Long,
    val productName: String,
    val quantity: Int,
    val requirements: String? = null,
    val deadline: LocalDate? = null
)

// 주문 응답
data class OrderResponse(
    val id: Long,
    val orderNumber: String?,
    val adTaskId: Long,
    val adTaskTitle: String,
    val memberId: Long,
    val memberCompanyName: String,
    val memberEmail: String,
    val productName: String,
    val quantity: Int,
    val requirements: String?,
    val deadline: LocalDate?,
    val startDate: LocalDate?,
    val completionDate: LocalDate?,
    val failureDate: LocalDate?,
    val progressRate: Int,
    val failureReason: String?,
    val status: OrderStatus,
    val submittedAt: LocalDateTime,
    val reviewedAt: LocalDateTime?,
    val reviewedByName: String?,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val paymentInfo: OrderPaymentInfo?,
    val roundParticipants: RoundParticipantsInfo?
)

// 주문 결제 정보
data class OrderPaymentInfo(
    val id: Long,
    val applicationNumber: String,
    val paymentAmount: BigDecimal,
    val depositorName: String?,
    val paymentStatus: PaymentStatus,
    val bankAccountNumber: String,
    val bankName: String,
    val paymentConfirmedAt: LocalDateTime?,
    val notes: String?
)

// 결제 생성 요청
data class PaymentCreateRequest(
    val orderId: Long,
    val paymentAmount: BigDecimal,
    val depositorName: String? = null,
    val bankAccountNumber: String,
    val bankName: String,
    val notes: String? = null
)

// 결제 상태 업데이트 요청
data class PaymentStatusUpdateRequest(
    val paymentStatus: PaymentStatus,
    val notes: String? = null
)

// 주문 상태 업데이트 요청
data class OrderStatusUpdateRequest(
    val status: OrderStatus,
    val notes: String? = null,
    val failureReason: String? = null,
    val progressRate: Int? = null
)


// 회원별 주문 목록
data class MemberOrderList(
    val memberId: Long,
    val memberCompanyName: String,
    val memberEmail: String,
    val totalOrders: Int,
    val orders: List<OrderResponse>
)

// 주문 수정 요청
data class OrderUpdateRequest(
    val deadline: LocalDate? = null,
    val requirements: String? = null
)

// 회원별 통계
data class MemberStatistics(
    val memberId: Long,
    val memberCompanyName: String,
    val memberEmail: String,
    val period: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val summary: StatisticsSummary,
    val monthlyTrend: List<MonthlyTrend>,
    val statusDistribution: List<StatusDistribution>,
    val recentOrders: List<RecentOrder>
)

data class StatisticsSummary(
    val totalOrders: Int,
    val pendingOrders: Int,
    val paymentWaitingOrders: Int,
    val paymentConfirmedOrders: Int,
    val approvedOrders: Int,
    val inProgressOrders: Int,
    val completedOrders: Int,
    val failedOrders: Int,
    val cancelledOrders: Int,
    val totalOrderAmount: BigDecimal,
    val averageOrderAmount: BigDecimal,
    val successRate: Double
)

data class MonthlyTrend(
    val month: String,
    val totalOrders: Long,
    val completedOrders: Long,
    val totalAmount: BigDecimal
)

data class StatusDistribution(
    val status: OrderStatus,
    val count: Long,
    val percentage: Double
)

data class RecentOrder(
    val id: Long,
    val orderNumber: String,
    val productName: String,
    val status: OrderStatus,
    val progressRate: Int,
    val submittedAt: LocalDateTime
)

// 전체 통계 (관리자용)
data class OverviewStatistics(
    val period: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val overallSummary: OverallSummary,
    val statusSummary: StatusSummary,
    val dailyTrend: List<DailyTrend>,
    val topPerformers: List<TopPerformer>
)

data class OverallSummary(
    val totalOrders: Int,
    val totalMembers: Int,
    val totalOrderAmount: BigDecimal,
    val averageOrderAmount: BigDecimal,
    val successRate: Double,
    val completionRate: Double
)

data class StatusSummary(
    val pendingOrders: Int,
    val paymentWaitingOrders: Int,
    val paymentConfirmedOrders: Int,
    val approvedOrders: Int,
    val inProgressOrders: Int,
    val completedOrders: Int,
    val failedOrders: Int,
    val cancelledOrders: Int
)

data class DailyTrend(
    val date: LocalDate,
    val newOrders: Long,
    val completedOrders: Long
)

data class TopPerformer(
    val memberId: Long,
    val memberCompanyName: String,
    val totalOrders: Int,
    val completedOrders: Int,
    val totalAmount: BigDecimal
)

// 라운드 참여자 정보
data class RoundParticipantsInfo(
    val roundId: Long,
    val totalParticipants: Int,
    val participants: List<RoundParticipant>
)

data class RoundParticipant(
    val companyName: String,
    val orderDate: LocalDateTime,
    val orderStatus: OrderStatus
)

// 테스트용 결제 생성 요청 (orderId 없이)
data class TestPaymentCreateRequest(
    val paymentAmount: BigDecimal,
    val depositorName: String? = null,
    val bankAccountNumber: String,
    val bankName: String,
    val notes: String? = null
)

// 광고 구매 요청 (로그인한 유저가 특정 라운드의 광고를 구매)
data class AdPurchaseRequest(
    val adTaskId: Long,
    val memberId: Long,
    val quantity: Int = 1,
    val requirements: String? = null
)

// 광고 구매 응답
data class AdPurchaseResponse(
    val order: OrderResponse,
    val payment: OrderPaymentInfo
)

// 내가 구매한 광고 목록 응답 (통합 버전)
data class MyPurchaseResponse(
    val orderId: Long,
    val orderNumber: String,
    val roundId: Long,
    val adTaskId: Long,
    val productName: String,
    val quantity: Int,
    val orderStatus: OrderStatus,
    val orderDate: LocalDateTime,
    val paymentStatus: PaymentStatus?,
    val paymentAmount: BigDecimal,
    val deadline: LocalDate?,
    val requirements: String?,
    val totalParticipants: Int,
    val roundTitle: String?,
    val adUrl: String?, // 광고 URL
    val receiptInfo: ReceiptSummary // 영수증 요약 정보
)

// 영수증 요약 정보
data class ReceiptSummary(
    val receiptNumber: String,
    val totalAmount: BigDecimal,
    val vatAmount: BigDecimal,
    val finalAmount: BigDecimal,
    val companyName: String = "CNC"
)

// 주문 영수증 상세 응답
data class OrderReceiptResponse(
    val receiptNumber: String,
    val orderNumber: String,
    val issueDate: LocalDateTime,
    val customerInfo: CustomerInfo,
    val orderDetails: OrderDetails,
    val paymentInfo: PaymentInfo,
    val companyInfo: CompanyInfo,
    val totalAmount: BigDecimal,
    val vatAmount: BigDecimal,
    val finalAmount: BigDecimal
)

// 고객 정보
data class CustomerInfo(
    val companyName: String,
    val email: String,
    val contactNumber: String?,
    val businessRegistrationNumber: String?
)

// 주문 상세 정보
data class OrderDetails(
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
    val orderDate: LocalDateTime,
    val deadline: LocalDate?,
    val requirements: String?,
    val roundId: Long,
    val adTaskId: Long
)

// 결제 정보
data class PaymentInfo(
    val paymentMethod: String,
    val paymentStatus: PaymentStatus,
    val paymentDate: LocalDateTime?,
    val depositorName: String?,
    val bankName: String,
    val accountNumber: String,
    val applicationNumber: String?
)

// 회사 정보
data class CompanyInfo(
    val companyName: String = "KoSpring",
    val businessNumber: String = "123-45-67890",
    val address: String = "서울시 강남구 테헤란로 123",
    val phone: String = "02-1234-5678",
    val email: String = "contact@kospring.com"
)

// 영수증 목록용 요약 정보
data class OrderReceiptSummary(
    val orderId: Long,
    val receiptNumber: String,
    val orderNumber: String,
    val productName: String,
    val totalAmount: BigDecimal,
    val paymentStatus: PaymentStatus,
    val issueDate: LocalDateTime,
    val orderDate: LocalDateTime
)