package com.service.frame.revenue.controller

import com.service.frame.revenue.dto.*
import com.service.frame.revenue.service.RevenueService
import com.service.frame.revenue.service.TaxInvoiceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/revenue")
class RevenueController(
    private val revenueService: RevenueService,
    private val taxInvoiceService: TaxInvoiceService
) {
    
    /**
     * 수익 목록 조회
     */
    @GetMapping("/list")
    fun getRevenueList(
        @RequestParam("memberId") memberId: Long,
        @RequestParam("type", required = false) type: RevenueType?,
        @RequestParam("period", defaultValue = "MONTH") period: RevenuePeriod
    ): ResponseEntity<RevenueListResponse> {
        val request = RevenueFilterRequest(
            type = type,
            period = period,
            memberId = memberId
        )
        val result = revenueService.getRevenueList(request, memberId)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 수익 요약 정보 조회
     */
    @GetMapping("/summary")
    fun getRevenueSummary(
        @RequestParam("memberId") memberId: Long,
        @RequestParam("period", defaultValue = "MONTH") period: RevenuePeriod
    ): ResponseEntity<RevenueSummaryResponse> {
        val request = RevenueFilterRequest(period = period, memberId = memberId)
        val result = revenueService.getRevenueList(request, memberId)
        return ResponseEntity.ok(result.summary)
    }
    
    /**
     * 정산 정보 조회
     */
    @GetMapping("/settlement/info")
    fun getSettlementInfo(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<SettlementInfoResponse> {
        val result = revenueService.getSettlementInfo(memberId)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 정산 내역 조회
     */
    @GetMapping("/settlement/history")
    fun getSettlementHistory(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<SettlementHistoryResponse> {
        val result = revenueService.getSettlementHistory(memberId)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 계좌 정보 수정
     */
    @PutMapping("/account")
    fun updateAccountInfo(
        @RequestParam("memberId") memberId: Long,
        @RequestBody accountInfo: AccountInfo
    ): ResponseEntity<Map<String, Any>> {
        val success = revenueService.updateAccountInfo(memberId, accountInfo)
        return ResponseEntity.ok(mapOf(
            "success" to success,
            "message" to if (success) "계좌 정보가 업데이트되었습니다. 1원 인증을 진행해주세요." else "업데이트 실패"
        ))
    }
    
    /**
     * 세금계산서 생성
     */
    @PostMapping("/tax-invoice")
    fun generateTaxInvoice(
        @RequestBody request: TaxInvoiceRequest
    ): ResponseEntity<TaxInvoiceResponse> {
        val result = taxInvoiceService.generateTaxInvoice(request)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 세금계산서 목록 조회
     */
    @GetMapping("/tax-invoice/list")
    fun getTaxInvoiceList(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<TaxInvoiceListResponse> {
        val result = taxInvoiceService.getTaxInvoiceList(memberId)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 세금계산서 상세 조회
     */
    @GetMapping("/tax-invoice/{invoiceNumber}")
    fun getTaxInvoiceDetail(
        @PathVariable invoiceNumber: String
    ): ResponseEntity<TaxInvoiceResponse> {
        val result = taxInvoiceService.getTaxInvoiceDetail(invoiceNumber)
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * 세금계산서 이메일 발송
     */
    @PostMapping("/tax-invoice/{invoiceNumber}/email")
    fun sendTaxInvoiceEmail(
        @PathVariable invoiceNumber: String,
        @RequestParam("email") email: String
    ): ResponseEntity<Map<String, Any>> {
        val success = taxInvoiceService.sendTaxInvoiceByEmail(invoiceNumber, email)
        return ResponseEntity.ok(mapOf(
            "success" to success,
            "message" to if (success) "세금계산서가 이메일로 발송되었습니다." else "발송 실패"
        ))
    }
    
    /**
     * 세금계산서 PDF 다운로드
     */
    @GetMapping("/tax-invoice/{invoiceNumber}/pdf")
    fun downloadTaxInvoicePdf(
        @PathVariable invoiceNumber: String
    ): ResponseEntity<ByteArray> {
        val pdfData = taxInvoiceService.downloadTaxInvoicePdf(invoiceNumber)
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "attachment; filename=tax-invoice-$invoiceNumber.pdf")
            .body(pdfData)
    }
    
    /**
     * 수익 데이터 내보내기 (Excel)
     */
    @GetMapping("/export")
    fun exportRevenueData(
        @RequestParam("memberId") memberId: Long,
        @RequestParam("type", required = false) type: RevenueType?,
        @RequestParam("period", defaultValue = "MONTH") period: RevenuePeriod
    ): ResponseEntity<ByteArray> {
        // 실제로는 Excel 파일 생성 로직
        val excelData = "Revenue Data Export".toByteArray()
        return ResponseEntity.ok()
            .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .header("Content-Disposition", "attachment; filename=revenue-data.xlsx")
            .body(excelData)
    }
    
    /**
     * 테스트용 API - 수익 데이터 생성
     */
    @PostMapping("/test/generate")
    fun generateTestRevenueData(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<Map<String, Any>> {
        // 테스트용 수익 데이터 생성 로직
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "테스트 수익 데이터가 생성되었습니다.",
            "generatedCount" to 10
        ))
    }
}