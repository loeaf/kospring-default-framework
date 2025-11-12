package com.service.frame.order.controller

import com.service.frame.order.dto.*
import com.service.frame.order.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {

    /**
     * 주문 생성
     */
    @PostMapping("/members/{memberId}")
    fun createOrder(
        @PathVariable memberId: Long,
        @RequestBody request: OrderCreateRequest
    ): ResponseEntity<OrderResponse> {
        val result = orderService.createOrder(memberId, request)
        return ResponseEntity.ok(result)
    }

    /**
     * 특정 주문 조회
     */
    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: Long): ResponseEntity<OrderResponse> {
        val result = orderService.getOrder(orderId)
        return ResponseEntity.ok(result)
    }

    /**
     * 주문번호로 주문 조회
     */
    @GetMapping("/number/{orderNumber}")
    fun getOrderByNumber(@PathVariable orderNumber: String): ResponseEntity<OrderResponse> {
        val result = orderService.getOrderByNumber(orderNumber)
        return ResponseEntity.ok(result)
    }

    /**
     * 회원별 주문 목록 조회
     */
    @GetMapping("/members/{memberId}")
    fun getMemberOrders(@PathVariable memberId: Long): ResponseEntity<MemberOrderList> {
        val result = orderService.getMemberOrders(memberId)
        return ResponseEntity.ok(result)
    }

    /**
     * 주문 상태 업데이트
     */
    @PutMapping("/{orderId}/status")
    fun updateOrderStatus(
        @PathVariable orderId: Long,
        @RequestBody request: OrderStatusUpdateRequest
    ): ResponseEntity<OrderResponse> {
        val result = orderService.updateOrderStatus(orderId, request)
        return ResponseEntity.ok(result)
    }

    /**
     * 결제 정보 생성
     */
    @PostMapping("/payments")
    fun createPayment(@RequestBody request: PaymentCreateRequest): ResponseEntity<OrderPaymentInfo> {
        val result = orderService.createPayment(request)
        return ResponseEntity.ok(result)
    }

    /**
     * 결제 상태 업데이트
     */
    @PutMapping("/payments/{paymentId}/status")
    fun updatePaymentStatus(
        @PathVariable paymentId: Long,
        @RequestBody request: PaymentStatusUpdateRequest
    ): ResponseEntity<OrderPaymentInfo> {
        val result = orderService.updatePaymentStatus(paymentId, request)
        return ResponseEntity.ok(result)
    }

    /**
     * 주문별 결제 내역 조회
     */
    @GetMapping("/{orderId}/payments")
    fun getPaymentsByOrder(@PathVariable orderId: Long): ResponseEntity<List<OrderPaymentInfo>> {
        val result = orderService.getPaymentsByOrder(orderId)
        return ResponseEntity.ok(result)
    }

    /**
     * 입금 대기중인 결제 목록 조회 (관리자용)
     */
    @GetMapping("/payments/pending")
    fun getPendingPayments(): ResponseEntity<List<OrderPaymentInfo>> {
        val result = orderService.getPendingPayments()
        return ResponseEntity.ok(result)
    }

    /**
     * 주문 정보 수정
     */
    @PutMapping("/{orderId}")
    fun updateOrder(
        @PathVariable orderId: Long,
        @RequestBody request: OrderUpdateRequest
    ): ResponseEntity<OrderResponse> {
        val result = orderService.updateOrder(orderId, request)
        return ResponseEntity.ok(result)
    }

    /**
     * 회원별 주문 통계 조회
     */
    @GetMapping("/members/{memberId}/statistics")
    fun getMemberStatistics(
        @PathVariable memberId: Long,
        @RequestParam(defaultValue = "thisMonth") period: String
    ): ResponseEntity<MemberStatistics> {
        val result = orderService.getMemberStatistics(memberId, period)
        return ResponseEntity.ok(result)
    }

    /**
     * 테스트용 API - 특정 라운드의 ad_index=1인 AdTask로 주문 생성
     */
    @PostMapping("/test/round/{roundId}")
    fun createTestOrder(
        @PathVariable roundId: Int,
        @RequestBody request: OrderCreateRequest
    ): ResponseEntity<List<OrderResponse>> {
        val result = orderService.createTestOrderWithRound(roundId, request)
        return ResponseEntity.ok(result)
    }

    /**
     * 테스트용 API - 특정 라운드의 주문에 대한 결제 생성
     */
    @PostMapping("/test/payments/round/{roundId}")
    fun createTestPayment(
        @PathVariable roundId: Int,
        @RequestBody request: TestPaymentCreateRequest
    ): ResponseEntity<List<OrderPaymentInfo>> {
        val result = orderService.createTestPaymentWithRound(roundId, request)
        return ResponseEntity.ok(result)
    }

    /**
     * 테스트용 API - 특정 라운드의 모든 결제를 일괄 승인
     */
    @PutMapping("/test/payments/round/{roundId}/confirm-all")
    fun confirmAllPaymentsByRound(
        @PathVariable roundId: Int,
        @RequestParam(defaultValue = "라운드별 일괄 입금 확인") notes: String
    ): ResponseEntity<List<OrderPaymentInfo>> {
        val result = orderService.confirmAllPaymentsByRound(roundId, notes)
        return ResponseEntity.ok(result)
    }

    /**
     * 테스트용 API - 특정 라운드의 특정 멤버 결제를 승인
     */
    @PutMapping("/test/payments/round/{roundId}/member/{memberId}/confirm")
    fun confirmPaymentsByRoundAndMember(
        @PathVariable roundId: Int,
        @PathVariable memberId: Long,
        @RequestParam(defaultValue = "멤버별 입금 확인") notes: String
    ): ResponseEntity<List<OrderPaymentInfo>> {
        val result = orderService.confirmPaymentsByRoundAndMember(roundId, memberId, notes)
        return ResponseEntity.ok(result)
    }

    /**
     * 라운드 전체 정보 조회 (AdTasks, Orders, Payments 포함)
     */
    @GetMapping("/round/{roundId}/info")
    fun getRoundInfo(@PathVariable roundId: Int): ResponseEntity<RoundInfoResponse> {
        val result = orderService.getRoundInfo(roundId)
        return ResponseEntity.ok(result)
    }

    /**
     * 라운드 키 정보 조회 (ID 값들만 간단히)
     */
    @GetMapping("/round/{roundId}/keys")
    fun getRoundKeys(@PathVariable roundId: Int): ResponseEntity<RoundKeysResponse> {
        val result = orderService.getRoundKeys(roundId)
        return ResponseEntity.ok(result)
    }

    /**
     * 전체 주문 통계 조회 (관리자용)
     */
    @GetMapping("/statistics/overview")
    fun getOverviewStatistics(
        @RequestParam(defaultValue = "thisMonth") period: String
    ): ResponseEntity<OverviewStatistics> {
        val result = orderService.getOverviewStatistics(period)
        return ResponseEntity.ok(result)
    }

    /**
     * 라운드의 모든 포스트가 게시 완료되었을 때 주문 상태를 완료로 업데이트
     */
    @PutMapping("/round/{roundId}/complete-if-posts-published")
    fun completeOrdersIfAllPostsPublished(
        @PathVariable roundId: Long,
        @RequestParam(defaultValue = "모든 포스트 게시 완료로 인한 주문 완료") notes: String
    ): ResponseEntity<List<OrderResponse>> {
        val result = orderService.completeOrdersIfAllPostsPublished(roundId, notes)
        return ResponseEntity.ok(result)
    }

    /**
     * 라운드의 주문 진행률 업데이트 (포스트 게시 상태 기반)
     */
    @PutMapping("/round/{roundId}/update-progress")
    fun updateOrderProgressByRound(
        @PathVariable roundId: Long
    ): ResponseEntity<List<OrderResponse>> {
        val result = orderService.updateOrderProgressByRound(roundId)
        return ResponseEntity.ok(result)
    }

    /**
     * 라운드의 입금 확인된 주문들을 광고 게시중 상태로 변경
     */
    @PutMapping("/round/{roundId}/start-advertising")
    fun startAdvertisingForRound(
        @PathVariable roundId: Long,
        @RequestParam(defaultValue = "입금 확인 완료, 광고 게시 시작") notes: String
    ): ResponseEntity<List<OrderResponse>> {
        val result = orderService.startAdvertisingForRound(roundId, notes)
        return ResponseEntity.ok(result)
    }

    /**
     * 로그인한 유저가 특정 라운드의 광고를 구매하는 API (주문 생성 + 결제 생성)
     */
    @PostMapping("/purchase")
    fun purchaseAd(
        @RequestBody request: AdPurchaseRequest
    ): ResponseEntity<AdPurchaseResponse> {
        val result = orderService.purchaseAd(request)
        return ResponseEntity.ok(result)
    }

    /**
     * 내가 구매한 광고 목록 조회 (영수증 정보 포함)
     */
    @GetMapping("/members/{memberId}/my-purchases")
    fun getMyPurchases(@PathVariable memberId: Long): ResponseEntity<List<MyPurchaseResponse>> {
        val result = orderService.getMyPurchases(memberId)
        return ResponseEntity.ok(result)
    }

    /**
     * 광고 URL 다운로드 (octet-stream)
     */
    @GetMapping("/orders/{orderId}/ad-url/download")
    fun downloadAdUrl(@PathVariable orderId: Long): ResponseEntity<Any> {
        val urlContent = orderService.downloadAdUrl(orderId)
        val headers = org.springframework.http.HttpHeaders()
        headers.add("Content-Disposition", "attachment; filename=ad-url-${orderId}.html")
        headers.add("Content-Type", "application/octet-stream")
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(urlContent)
    }

    /**
     * 주문 영수증 다운로드 (HTML)
     */
    @GetMapping("/orders/{orderId}/receipt/download")
    fun downloadOrderReceipt(@PathVariable orderId: Long): ResponseEntity<Any> {
        val receiptContent = orderService.downloadOrderReceipt(orderId)
        val headers = org.springframework.http.HttpHeaders()
        headers.add("Content-Disposition", "attachment; filename=receipt-${orderId}.html")
        headers.add("Content-Type", "application/octet-stream")
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(receiptContent)
    }
}