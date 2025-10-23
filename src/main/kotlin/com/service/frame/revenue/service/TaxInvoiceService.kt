package com.service.frame.revenue.service

import com.service.frame.revenue.dto.*
import com.service.frame.revenue.repository.RevenueTransactionRepository
import com.service.frame.member.entity.Member
import com.service.frame.order.entity.Order
import com.service.frame.ad.entity.AdTask
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Service
@Transactional(readOnly = true)
class TaxInvoiceService(
    private val revenueTransactionRepository: RevenueTransactionRepository
) {
    
    fun generateTaxInvoice(request: TaxInvoiceRequest): TaxInvoiceResponse {
        val invoiceNumber = generateInvoiceNumber()
        val approvalNo = generateApprovalNumber()
        
        // 금액 계산 (10% 부가세)
        val supplyAmount = getRevenueItemAmount(request.revenueItemId)
        val taxAmount = supplyAmount.multiply(BigDecimal("0.1")).setScale(0, RoundingMode.HALF_UP)
        val totalAmount = supplyAmount.add(taxAmount)
        
        val item = getRevenueItemInfo(request.revenueItemId)
        
        return TaxInvoiceResponse(
            invoiceNumber = invoiceNumber,
            approvalNo = approvalNo,
            type = request.type,
            issueDate = LocalDateTime.now(),
            supplier = request.supplierInfo,
            buyer = request.buyerInfo,
            item = item.copy(unitPrice = supplyAmount),
            amounts = InvoiceAmounts(
                supply = supplyAmount,
                tax = taxAmount,
                total = totalAmount
            ),
            remarks = item.name,
            paymentMethod = PaymentMethod(
                cash = totalAmount,
                check = BigDecimal.ZERO,
                promissoryNote = BigDecimal.ZERO,
                credit = BigDecimal.ZERO
            )
        )
    }
    
    fun generateTaxInvoiceFromRevenueTransaction(revenueTransactionId: Long): TaxInvoiceResponse {
        val transaction = revenueTransactionRepository.findById(revenueTransactionId)
            .orElseThrow { IllegalArgumentException("Revenue transaction not found with id: $revenueTransactionId") }
        
        val invoiceNumber = generateInvoiceNumber()
        val approvalNo = generateApprovalNumber()
        
        // revenue transaction의 금액을 공급가액으로 사용
        val supplyAmount = transaction.amount ?: BigDecimal.ZERO
        val taxAmount = supplyAmount.multiply(BigDecimal("0.1")).setScale(0, RoundingMode.HALF_UP)
        val totalAmount = supplyAmount.add(taxAmount)
        
        // 거래 유형에 따라 공급자/구매자 및 항목 결정
        val (supplier, buyer, item, taxType) = when (transaction.transactionType) {
            com.service.frame.revenue.entity.TransactionType.EXPENSE -> {
                // 매입: 회원이 구매자, CNC가 공급자
                val memberInfo = createBusinessInfoFromMember(transaction.member!!)
                val cncInfo = createCNCBusinessInfo()
                val orderItem = createOrderBasedItem(transaction.order!!, supplyAmount)
                Tuple4(cncInfo, memberInfo, orderItem, TaxInvoiceType.PURCHASES)
            }
            com.service.frame.revenue.entity.TransactionType.INCOME -> {
                // 매출: 회원이 공급자, CNC가 구매자
                val memberInfo = createBusinessInfoFromMember(transaction.member!!)
                val cncInfo = createCNCBusinessInfo()
                val adTaskItem = createAdTaskBasedItem(transaction.assignment!!.adTask!!, supplyAmount)
                Tuple4(memberInfo, cncInfo, adTaskItem, TaxInvoiceType.SALES)
            }
            null -> {
                throw IllegalArgumentException("Transaction type is null for revenue transaction: $revenueTransactionId")
            }
        }
        
        return TaxInvoiceResponse(
            invoiceNumber = invoiceNumber,
            approvalNo = approvalNo,
            type = taxType,
            issueDate = LocalDateTime.now(),
            supplier = supplier,
            buyer = buyer,
            item = item,
            amounts = InvoiceAmounts(
                supply = supplyAmount,
                tax = taxAmount,
                total = totalAmount
            ),
            remarks = "${transaction.title} - ${transaction.transactionType} (Round #${transaction.roundNumber})",
            paymentMethod = PaymentMethod(
                cash = totalAmount,
                check = BigDecimal.ZERO,
                promissoryNote = BigDecimal.ZERO,
                credit = BigDecimal.ZERO
            )
        )
    }
    
    fun getTaxInvoiceList(memberId: Long): TaxInvoiceListResponse {
        // 임시 데이터 - 실제로는 세금계산서 테이블에서 조회
        val invoices = listOf(
            TaxInvoiceSummary(
                invoiceNumber = "202408230001",
                type = TaxInvoiceType.SALES,
                issueDate = LocalDateTime.of(2024, 8, 23, 10, 30),
                supplierCompany = "김도현",
                buyerCompany = "CNC 네트워크",
                totalAmount = BigDecimal("3520000"),
                status = TaxInvoiceStatus.ISSUED
            ),
            TaxInvoiceSummary(
                invoiceNumber = "202408220002",
                type = TaxInvoiceType.PURCHASES,
                issueDate = LocalDateTime.of(2024, 8, 22, 14, 15),
                supplierCompany = "CNC 네트워크",
                buyerCompany = "김도현",
                totalAmount = BigDecimal("1320000"),
                status = TaxInvoiceStatus.SENT
            )
        )
        
        return TaxInvoiceListResponse(
            invoices = invoices,
            totalCount = invoices.size
        )
    }
    
    fun getTaxInvoiceDetail(invoiceNumber: String): TaxInvoiceResponse? {
        // 임시 데이터 - 실제로는 DB에서 조회
        if (invoiceNumber == "202408230001") {
            return TaxInvoiceResponse(
                invoiceNumber = invoiceNumber,
                approvalNo = "2024082300000123",
                type = TaxInvoiceType.SALES,
                issueDate = LocalDateTime.of(2024, 8, 23, 10, 30),
                supplier = BusinessInfo(
                    businessNo = "234-56-78901",
                    company = "IT 개발회사",
                    ceo = "대표자명",
                    address = "서울시 강남구",
                    businessType = "서비스업",
                    businessItem = "소프트웨어 개발"
                ),
                buyer = BusinessInfo(
                    businessNo = "117-81-49125",
                    company = "CNC 네트워크",
                    ceo = "대표자명",
                    address = "서울시 서초구",
                    businessType = "서비스업",
                    businessItem = "플랫폼 운영"
                ),
                item = InvoiceItem(
                    name = "교육 플랫폼 UI 제작 용역",
                    specification = "",
                    quantity = 1,
                    unitPrice = BigDecimal("3200000"),
                    amount = BigDecimal("3200000")
                ),
                amounts = InvoiceAmounts(
                    supply = BigDecimal("3200000"),
                    tax = BigDecimal("320000"),
                    total = BigDecimal("3520000")
                ),
                remarks = "교육 플랫폼 UI 제작 용역",
                paymentMethod = PaymentMethod(
                    cash = BigDecimal("3520000"),
                    check = BigDecimal.ZERO,
                    promissoryNote = BigDecimal.ZERO,
                    credit = BigDecimal.ZERO
                )
            )
        }
        return null
    }
    
    @Transactional
    fun sendTaxInvoiceByEmail(invoiceNumber: String, email: String): Boolean {
        // 실제로는 이메일 발송 로직
        println("세금계산서 $invoiceNumber 를 $email 로 발송합니다.")
        return true
    }
    
    fun downloadTaxInvoicePdf(invoiceNumber: String): ByteArray {
        // 실제로는 PDF 생성 로직
        return "PDF Content".toByteArray()
    }
    
    private fun generateInvoiceNumber(): String {
        val now = LocalDateTime.now()
        val dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val sequenceStr = String.format("%04d", (Math.random() * 9999).toInt() + 1)
        return "$dateStr$sequenceStr"
    }
    
    private fun generateApprovalNumber(): String {
        val now = LocalDateTime.now()
        val dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val sequenceStr = String.format("%08d", (Math.random() * 99999999).toInt() + 1)
        return "$dateStr$sequenceStr"
    }
    
    private fun getRevenueItemAmount(revenueItemId: String): BigDecimal {
        // 실제로는 수익 아이템에서 금액 조회
        return when {
            revenueItemId.startsWith("ad_") -> BigDecimal("125000")
            revenueItemId.startsWith("order_") -> BigDecimal("3200000")
            else -> BigDecimal("100000")
        }
    }
    
    private fun getRevenueItemInfo(revenueItemId: String): InvoiceItem {
        // 실제로는 수익 아이템에서 정보 조회
        return when {
            revenueItemId.startsWith("ad_") -> InvoiceItem(
                name = "헬스케어 광고 수익",
                specification = "",
                quantity = 1,
                unitPrice = BigDecimal("125000"),
                amount = BigDecimal("125000")
            )
            revenueItemId.startsWith("order_") -> InvoiceItem(
                name = "교육 플랫폼 UI 제작 용역",
                specification = "",
                quantity = 1,
                unitPrice = BigDecimal("3200000"),
                amount = BigDecimal("3200000")
            )
            else -> InvoiceItem(
                name = "기타 수익",
                specification = "",
                quantity = 1,
                unitPrice = BigDecimal("100000"),
                amount = BigDecimal("100000")
            )
        }
    }
    
    private fun createBusinessInfoFromMember(member: Member): BusinessInfo {
        return BusinessInfo(
            businessNo = member.businessRegistrationNumber,
            company = member.companyName,
            ceo = "대표자",
            address = "주소 미등록",
            businessType = "서비스업",
            businessItem = "광고 서비스"
        )
    }
    
    private fun createCNCBusinessInfo(): BusinessInfo {
        return BusinessInfo(
            businessNo = "117-81-49125",
            company = "CNC 네트워크",
            ceo = "대표자명",
            address = "서울시 서초구",
            businessType = "서비스업",
            businessItem = "플랫폼 운영"
        )
    }
    
    private fun createOrderBasedItem(order: Order, amount: BigDecimal): InvoiceItem {
        return InvoiceItem(
            name = "${order.productName} 광고 게시 서비스",
            specification = "Round #${order.adTask.round.id} - ${order.adTask.round.title}",
            quantity = 1,
            unitPrice = amount,
            amount = amount
        )
    }
    
    private fun createAdTaskBasedItem(adTask: AdTask, amount: BigDecimal): InvoiceItem {
        return InvoiceItem(
            name = "광고 콘텐츠 제작 및 게시",
            specification = "Round #${adTask.round.id} - ${adTask.round.title} (${adTask.adType})",
            quantity = 1,
            unitPrice = amount,
            amount = amount
        )
    }
}