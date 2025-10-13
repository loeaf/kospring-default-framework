package com.service.frame.revenue.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class RevenueItemResponse(
    val id: String,
    val type: RevenueType,
    val title: String,
    val description: String,
    val date: LocalDateTime,
    val amount: BigDecimal,
    val status: String,
    val fee: BigDecimal,
    val metrics: List<RevenueMetric>,
    val category: String? = null
)

data class RevenueMetric(
    val label: String,
    val value: String
)

enum class RevenueType {
    SALES,      // 매출 (광고 수익)
    PURCHASES   // 매입 (주문 비용)
}

data class RevenueSummaryResponse(
    val totalRevenue: BigDecimal,
    val monthlyGrowth: BigDecimal,
    val adRevenue: BigDecimal,
    val adRevenueGrowth: BigDecimal,
    val orderRevenue: BigDecimal,
    val orderRevenueGrowth: BigDecimal,
    val period: String
)

data class RevenueListResponse(
    val items: List<RevenueItemResponse>,
    val totalCount: Int,
    val summary: RevenueSummaryResponse
)

data class RevenueFilterRequest(
    val type: RevenueType? = null,
    val period: RevenuePeriod = RevenuePeriod.MONTH,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val memberId: Long? = null
)

enum class RevenuePeriod {
    WEEK,
    MONTH,
    QUARTER,
    YEAR
}

data class SettlementInfoResponse(
    val nextSettlementDate: LocalDateTime,
    val expectedAmount: BigDecimal,
    val totalSettled: BigDecimal,
    val accountInfo: AccountInfo
)

data class AccountInfo(
    val bankName: String,
    val accountNumber: String,
    val accountHolder: String
)

data class SettlementHistoryResponse(
    val settlements: List<SettlementItem>,
    val totalCount: Int
)

data class SettlementItem(
    val id: Long,
    val period: String,
    val amount: BigDecimal,
    val settlementDate: LocalDateTime,
    val status: SettlementStatus
)

enum class SettlementStatus {
    PENDING,    // 대기
    COMPLETED,  // 완료
    FAILED      // 실패
}