package com.service.frame.revenue.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class TaxInvoiceResponse(
    val invoiceNumber: String,
    val type: TaxInvoiceType,
    val issueDate: LocalDateTime,
    val supplier: BusinessInfo,
    val buyer: BusinessInfo,
    val item: InvoiceItem,
    val amounts: InvoiceAmounts,
    val approvalNo: String? = null
)

enum class TaxInvoiceType {
    SALES,      // 매출
    PURCHASES   // 매입
}

data class BusinessInfo(
    val businessNo: String,
    val company: String,
    val ceo: String,
    val address: String? = null,
    val businessType: String? = null,
    val businessItem: String? = null
)

data class InvoiceItem(
    val name: String,
    val specification: String? = null,
    val quantity: Int = 1,
    val unitPrice: BigDecimal? = null,
    val amount: BigDecimal
)

data class InvoiceAmounts(
    val supply: BigDecimal,     // 공급가액
    val tax: BigDecimal,        // 세액
    val total: BigDecimal       // 합계
)

data class TaxInvoiceRequest(
    val revenueItemId: String,
    val type: TaxInvoiceType,
    val supplierInfo: BusinessInfo,
    val buyerInfo: BusinessInfo
)

data class TaxInvoiceListResponse(
    val invoices: List<TaxInvoiceSummary>,
    val totalCount: Int
)

data class TaxInvoiceSummary(
    val invoiceNumber: String,
    val type: TaxInvoiceType,
    val issueDate: LocalDateTime,
    val supplierCompany: String,
    val buyerCompany: String,
    val totalAmount: BigDecimal,
    val status: TaxInvoiceStatus
)

enum class TaxInvoiceStatus {
    DRAFT,      // 임시저장
    ISSUED,     // 발행완료
    SENT,       // 전송완료
    FAILED      // 발행실패
}