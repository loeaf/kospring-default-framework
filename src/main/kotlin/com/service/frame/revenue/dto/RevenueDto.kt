package com.service.frame.revenue.dto

import com.service.frame.revenue.entity.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalDate

data class RevenueItemResponse(
    val id: Long,
    val transactionType: TransactionType,
    val title: String,
    val description: String,
    val transactionDate: LocalDate,
    val amount: BigDecimal,
    val memberCompanyName: String,
    val roundNumber: String? = null,
    val roundCategory: String? = null,
    val ctrRate: BigDecimal? = null,
    val orderNumber: String? = null,
    val orderStatus: String? = null,
    val taxInvoiceIssued: Boolean,
    val taxInvoiceNumber: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class RevenueType {
    SALES,      // 매출 (광고 수익)
    PURCHASES   // 매입 (주문 비용)
}

data class RevenueSummaryResponse(
    val totalIncome: BigDecimal,
    val totalExpense: BigDecimal,
    val netRevenue: BigDecimal,
    val monthlyGrowth: Double,
    val incomeGrowth: Double,
    val expenseGrowth: Double,
    val period: String
)

data class RevenueListResponse(
    val items: List<RevenueItemResponse>,
    val totalCount: Int,
    val summary: RevenueSummaryResponse
)

data class RevenueFilterRequest(
    val transactionType: TransactionType? = null,
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