package com.service.frame.order.service

import com.service.frame.order.dto.*
import com.service.frame.order.entity.Order
import com.service.frame.order.entity.OrderPayment
import com.service.frame.order.entity.OrderStatus
import com.service.frame.order.entity.PaymentStatus
import com.service.frame.order.repository.OrderRepository
import com.service.frame.order.repository.OrderPaymentRepository
import com.service.frame.member.repository.MemberRepository
import com.service.frame.ad.repository.AdTaskRepository
import com.service.frame.round.repository.RoundRepository
import com.service.frame.revenue.service.RevenueService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderPaymentRepository: OrderPaymentRepository,
    private val memberRepository: MemberRepository,
    private val adTaskRepository: AdTaskRepository,
    private val roundRepository: RoundRepository,
    private val revenueService: RevenueService
) {
    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    @Transactional
    fun createOrder(memberId: Long, request: OrderCreateRequest): OrderResponse {
        val member = memberRepository.findById(memberId).orElse(null)
            ?: throw IllegalArgumentException("Member not found with id: $memberId")
        
        val adTask = adTaskRepository.findById(request.adTaskId).orElse(null)
            ?: throw IllegalArgumentException("AdTask not found with id: ${request.adTaskId}")

        val orderNumber = generateOrderNumber()
        
        val order = Order(
            orderNumber = orderNumber,
            adTask = adTask,
            member = member,
            productName = request.productName,
            quantity = request.quantity,
            requirements = request.requirements,
            deadline = request.deadline,
            status = OrderStatus.PENDING
        )

        val savedOrder = orderRepository.save(order)
        return mapToOrderResponse(savedOrder)
    }

    @Transactional
    fun createPayment(request: PaymentCreateRequest): OrderPaymentInfo {
        val order = orderRepository.findById(request.orderId)
            .orElseThrow { IllegalArgumentException("Order not found with id: ${request.orderId}") }

        if (order.status != OrderStatus.PENDING) {
            throw IllegalStateException("Order must be in PENDING status to create payment")
        }

        val applicationNumber = generateApplicationNumber()
        
        val payment = OrderPayment(
            order = order,
            applicationNumber = applicationNumber,
            paymentAmount = request.paymentAmount,
            depositorName = request.depositorName,
            bankAccountNumber = request.bankAccountNumber,
            bankName = request.bankName,
            notes = request.notes,
            paymentStatus = PaymentStatus.WAITING
        )

        val savedPayment = orderPaymentRepository.save(payment)
        
        // 주문 상태를 PAYMENT_WAITING으로 변경
        updateOrderStatus(order.id!!, OrderStatusUpdateRequest(OrderStatus.PAYMENT_WAITING))
        
        return mapToOrderPaymentInfo(savedPayment)
    }

    @Transactional
    fun updatePaymentStatus(paymentId: Long, request: PaymentStatusUpdateRequest): OrderPaymentInfo {
        val payment = orderPaymentRepository.findById(paymentId).orElse(null)
            ?: throw IllegalArgumentException("Payment not found with id: $paymentId")

        val updatedPayment = payment.copy(
            paymentStatus = request.paymentStatus,
            notes = request.notes,
            paymentConfirmedAt = if (request.paymentStatus == PaymentStatus.CONFIRMED) LocalDateTime.now() else null,
            updatedAt = LocalDateTime.now()
        )

        val savedPayment = orderPaymentRepository.save(updatedPayment)

        // 결제 확인되면 주문 상태도 업데이트
        if (request.paymentStatus == PaymentStatus.CONFIRMED) {
            updateOrderStatus(payment.order.id!!, OrderStatusUpdateRequest(OrderStatus.PAYMENT_CONFIRMED))
        }

        return mapToOrderPaymentInfo(savedPayment)
    }

    @Transactional
    fun updateOrderStatus(orderId: Long, request: OrderStatusUpdateRequest): OrderResponse {
        val order = orderRepository.findById(orderId).orElse(null)
            ?: throw IllegalArgumentException("Order not found with id: $orderId")

        val updatedOrder = order.copy(
            status = request.status,
            notes = request.notes,
            failureReason = request.failureReason,
            progressRate = request.progressRate ?: order.progressRate,
            startDate = if (request.status == OrderStatus.IN_PROGRESS && order.startDate == null) 
                LocalDateTime.now().toLocalDate() else order.startDate,
            completionDate = if (request.status == OrderStatus.COMPLETED && order.completionDate == null) 
                LocalDateTime.now().toLocalDate() else order.completionDate,
            failureDate = if (request.status == OrderStatus.FAILED && order.failureDate == null) 
                LocalDateTime.now().toLocalDate() else order.failureDate,
            updatedAt = LocalDateTime.now()
        )

        val savedOrder = orderRepository.save(updatedOrder)
        
        // revenue_transactions 테이블의 order_status도 업데이트
        try {
            revenueService.updateOrderStatusInRevenueTransactions(savedOrder)
        } catch (e: Exception) {
            logger.warn("Failed to update revenue transaction order status for order ${savedOrder.id}: ${e.message}")
        }
        
        return mapToOrderResponse(savedOrder)
    }

    fun getOrder(orderId: Long): OrderResponse {
        val order = orderRepository.findById(orderId).orElse(null)
            ?: throw IllegalArgumentException("Order not found with id: $orderId")
        return mapToOrderResponse(order)
    }

    fun getOrderByNumber(orderNumber: String): OrderResponse {
        val order = orderRepository.findByOrderNumber(orderNumber)
            ?: throw IllegalArgumentException("Order not found with number: $orderNumber")
        return mapToOrderResponse(order)
    }

    fun getMemberOrders(memberId: Long): MemberOrderList {
        val member = memberRepository.findById(memberId).orElse(null)
            ?: throw IllegalArgumentException("Member not found with id: $memberId")

        val orders = orderRepository.findByMemberIdWithDetailsOrderBySubmittedAtDesc(memberId)
        
        val roundParticipantsCache = mutableMapOf<Long, List<Map<String, Any>>>()
        
        val orderResponses = orders.map { order ->
            val roundId = order.adTask?.round?.id
            val roundParticipants = if (roundId != null) {
                roundParticipantsCache.getOrPut(roundId) {
                    orderRepository.findRoundParticipants(roundId)
                }
            } else {
                emptyList()
            }
            
            mapToOrderResponseWithRoundParticipants(order, roundParticipants, memberId)
        }

        return MemberOrderList(
            memberId = memberId,
            memberCompanyName = member.companyName ?: "",
            memberEmail = member.email,
            totalOrders = orders.size,
            orders = orderResponses
        )
    }


    fun getPaymentsByOrder(orderId: Long): List<OrderPaymentInfo> {
        val payments = orderPaymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
        return payments.map { mapToOrderPaymentInfo(it) }
    }

    fun getPendingPayments(): List<OrderPaymentInfo> {
        val payments = orderPaymentRepository.findByPaymentStatusOrderByCreatedAtAsc(PaymentStatus.WAITING)
        return payments.map { mapToOrderPaymentInfo(it) }
    }

    private fun mapToOrderResponse(order: Order): OrderResponse {
        val latestPayment = orderPaymentRepository.findByOrderIdOrderByCreatedAtDesc(order.id!!)
            .firstOrNull()

        // Calculate payment amount from Round's cost fields
        val round = order.adTask.round
        val paymentAmount = round.orderAmount

        // Always provide payment info with hardcoded bank details for user deposit
        val paymentInfo = if (latestPayment != null) {
            mapToOrderPaymentInfo(latestPayment).copy(
                paymentAmount = paymentAmount,
                bankAccountNumber = "110-123-456789",
                bankName = "신한은행"
            )
        } else {
            // Create payment info for user deposit with hardcoded bank details
            OrderPaymentInfo(
                id = 0L,
                applicationNumber = "",
                paymentAmount = paymentAmount,
                depositorName = "",
                paymentStatus = PaymentStatus.WAITING,
                bankAccountNumber = "110-123-456789",
                bankName = "신한은행",
                paymentConfirmedAt = null,
                notes = "입금 후 결제 승인 요청 바랍니다"
            )
        }

        // Round 정보 생성
        val roundInfo = RoundInfo(
            id = round.id!!,
            title = round.title,
            description = round.description,
            category = round.category,
            orderAmount = round.orderAmount,
            status = round.status,
            startDate = round.startDate?.toLocalDate(),
            endDate = round.endDate?.toLocalDate(),
            postStartDate = round.postStartDate?.toLocalDate(),
            postEndDate = round.postEndDate?.toLocalDate(),
            postDurationDays = round.postDurationDays,
            maxParticipants = round.maxParticipants,
            currentParticipants = orderRepository.countByRoundId(round.id!!).toInt(),
            roundNumber = round.roundNumber
        )

        return OrderResponse(
            id = order.id!!,
            orderNumber = order.orderNumber,
            adTaskId = order.adTask.id!!,
            adTaskTitle = "AdTask #${order.adTask.id}",
            memberId = order.member.id!!,
            memberCompanyName = order.member.companyName ?: "",
            memberEmail = order.member.email,
            productName = order.productName,
            quantity = order.quantity,
            requirements = order.requirements,
            deadline = order.deadline,
            startDate = order.startDate,
            completionDate = order.completionDate,
            failureDate = order.failureDate,
            progressRate = order.progressRate,
            failureReason = order.failureReason,
            status = order.status,
            submittedAt = order.submittedAt,
            reviewedAt = order.reviewedAt,
            reviewedByName = order.reviewedBy?.companyName,
            notes = order.notes,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt,
            paymentInfo = paymentInfo,
            roundParticipants = null,
            roundInfo = roundInfo
        )
    }

    private fun mapToOrderResponseWithRoundParticipants(order: Order, roundParticipants: List<Map<String, Any>>, requestingMemberId: Long): OrderResponse {
        val latestPayment = orderPaymentRepository.findByOrderIdOrderByCreatedAtDesc(order.id!!)
            .firstOrNull()

        val round = order.adTask.round
        val paymentAmount = round.orderAmount

        val paymentInfo = if (latestPayment != null) {
            mapToOrderPaymentInfo(latestPayment).copy(
                paymentAmount = paymentAmount,
                bankAccountNumber = "110-123-456789",
                bankName = "신한은행"
            )
        } else {
            OrderPaymentInfo(
                id = 0L,
                applicationNumber = "",
                paymentAmount = paymentAmount,
                depositorName = "",
                paymentStatus = PaymentStatus.WAITING,
                bankAccountNumber = "110-123-456789",
                bankName = "신한은행",
                paymentConfirmedAt = null,
                notes = "입금 후 결제 승인 요청 바랍니다"
            )
        }

        val roundParticipantsInfo = if (roundParticipants.isNotEmpty()) {
            val otherParticipants = roundParticipants.filter { 
                val memberId = it["memberId"] as? Long
                memberId != null && memberId != requestingMemberId
            }
            RoundParticipantsInfo(
                roundId = round.id!!,
                totalParticipants = otherParticipants.size,
                participants = otherParticipants.map { 
                    RoundParticipant(
                        companyName = it["companyName"] as String,
                        orderDate = it["orderDate"] as LocalDateTime,
                        orderStatus = it["orderStatus"] as OrderStatus
                    )
                }
            )
        } else null

        // Round 정보 생성
        val roundInfo = RoundInfo(
            id = round.id!!,
            title = round.title,
            description = round.description,
            category = round.category,
            orderAmount = round.orderAmount,
            status = round.status,
            startDate = round.startDate?.toLocalDate(),
            endDate = round.endDate?.toLocalDate(),
            postStartDate = round.postStartDate?.toLocalDate(),
            postEndDate = round.postEndDate?.toLocalDate(),
            postDurationDays = round.postDurationDays,
            maxParticipants = round.maxParticipants,
            currentParticipants = orderRepository.countByRoundId(round.id!!).toInt(),
            roundNumber = round.roundNumber
        )

        return OrderResponse(
            id = order.id!!,
            orderNumber = order.orderNumber,
            adTaskId = order.adTask.id!!,
            adTaskTitle = "AdTask #${order.adTask.id}",
            memberId = order.member.id!!,
            memberCompanyName = order.member.companyName ?: "",
            memberEmail = order.member.email,
            productName = order.productName,
            quantity = order.quantity,
            requirements = order.requirements,
            deadline = order.deadline,
            startDate = order.startDate,
            completionDate = order.completionDate,
            failureDate = order.failureDate,
            progressRate = order.progressRate,
            failureReason = order.failureReason,
            status = order.status,
            submittedAt = order.submittedAt,
            reviewedAt = order.reviewedAt,
            reviewedByName = order.reviewedBy?.companyName,
            notes = order.notes,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt,
            paymentInfo = paymentInfo,
            roundParticipants = roundParticipantsInfo,
            roundInfo = roundInfo
        )
    }

    private fun mapToOrderPaymentInfo(payment: OrderPayment): OrderPaymentInfo {
        return OrderPaymentInfo(
            id = payment.id!!,
            applicationNumber = payment.applicationNumber,
            paymentAmount = payment.paymentAmount,
            depositorName = payment.depositorName,
            paymentStatus = payment.paymentStatus,
            bankAccountNumber = payment.bankAccountNumber,
            bankName = payment.bankName,
            paymentConfirmedAt = payment.paymentConfirmedAt,
            notes = payment.notes
        )
    }

    private fun generateOrderNumber(): String {
        val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"))
        val count = orderRepository.count() + 1
        return "ORD-$today-${count.toString().padStart(3, '0')}"
    }

    private fun generateApplicationNumber(): String {
        val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
        val count = orderPaymentRepository.countTodayPayments() + 1
        return "APP$today${count.toString().padStart(2, '0')}"
    }

    // 주문 수정 기능 추가
    @Transactional
    fun updateOrder(orderId: Long, request: OrderUpdateRequest): OrderResponse {
        val order = orderRepository.findById(orderId).orElse(null)
            ?: throw IllegalArgumentException("Order not found with id: $orderId")

        if (!order.canBeModified()) {
            throw IllegalStateException("Order cannot be modified in current status: ${order.status}")
        }

        val updatedOrder = order.copy(
            deadline = request.deadline ?: order.deadline,
            requirements = request.requirements ?: order.requirements,
            updatedAt = LocalDateTime.now()
        )

        val savedOrder = orderRepository.save(updatedOrder)
        return mapToOrderResponse(savedOrder)
    }

    // 통계 기능들
    fun getMemberStatistics(memberId: Long, period: String = "thisMonth"): MemberStatistics {
        val member = memberRepository.findById(memberId).orElse(null)
            ?: throw IllegalArgumentException("Member not found with id: $memberId")

        val (startDate, endDate) = getPeriodDates(period)
        val orders = orderRepository.findByMemberIdAndCreatedAtBetween(memberId, startDate, endDate)
        val allOrders = orderRepository.findByMemberIdOrderBySubmittedAtDesc(memberId)

        val statusDistribution = orderRepository.getStatusDistributionByMember(memberId)
        val monthlyTrend = orderRepository.getMonthlyTrendByMember(memberId, startDate, endDate)

        return MemberStatistics(
            memberId = memberId,
            memberCompanyName = member.companyName ?: "",
            memberEmail = member.email,
            period = period,
            periodStart = startDate.toLocalDate(),
            periodEnd = endDate.toLocalDate(),
            summary = StatisticsSummary(
                totalOrders = orders.size,
                pendingOrders = orders.count { it.status == OrderStatus.PENDING },
                paymentWaitingOrders = orders.count { it.status == OrderStatus.PAYMENT_WAITING },
                paymentConfirmedOrders = orders.count { it.status == OrderStatus.PAYMENT_CONFIRMED },
                approvedOrders = orders.count { it.status == OrderStatus.APPROVED },
                inProgressOrders = orders.count { it.status == OrderStatus.IN_PROGRESS },
                completedOrders = orders.count { it.status == OrderStatus.COMPLETED },
                failedOrders = orders.count { it.status == OrderStatus.FAILED },
                cancelledOrders = orders.count { it.status == OrderStatus.CANCELLED },
                totalOrderAmount = calculateTotalAmount(orders),
                averageOrderAmount = if (orders.isNotEmpty()) calculateTotalAmount(orders).divide(BigDecimal(orders.size)) else BigDecimal.ZERO,
                successRate = if (orders.isNotEmpty()) (orders.count { it.status == OrderStatus.COMPLETED }.toDouble() / orders.size * 100) else 0.0
            ),
            monthlyTrend = monthlyTrend.map { 
                MonthlyTrend(
                    month = "${it["year"]}-${String.format("%02d", it["month"])}", 
                    totalOrders = it["totalOrders"] as Long,
                    completedOrders = it["completedOrders"] as Long,
                    totalAmount = BigDecimal.ZERO // TODO: 결제 정보와 연계 필요
                )
            },
            statusDistribution = statusDistribution.map {
                StatusDistribution(
                    status = it["status"] as OrderStatus,
                    count = it["count"] as Long,
                    percentage = if (allOrders.isNotEmpty()) (it["count"] as Long).toDouble() / allOrders.size * 100 else 0.0
                )
            },
            recentOrders = allOrders.take(5).map {
                RecentOrder(
                    id = it.id!!,
                    orderNumber = it.orderNumber ?: "",
                    productName = it.productName,
                    status = it.status,
                    progressRate = it.progressRate,
                    submittedAt = it.submittedAt
                )
            }
        )
    }

    fun getOverviewStatistics(period: String = "thisMonth"): OverviewStatistics {
        val (startDate, endDate) = getPeriodDates(period)
        val orders = orderRepository.findByCreatedAtBetween(startDate, endDate)
        val dailyStats = orderRepository.getDailyOrderStats(startDate, endDate)

        return OverviewStatistics(
            period = period,
            periodStart = startDate.toLocalDate(),
            periodEnd = endDate.toLocalDate(),
            overallSummary = OverallSummary(
                totalOrders = orders.size,
                totalMembers = orders.map { it.member.id }.distinct().size,
                totalOrderAmount = calculateTotalAmount(orders),
                averageOrderAmount = if (orders.isNotEmpty()) calculateTotalAmount(orders).divide(BigDecimal(orders.size)) else BigDecimal.ZERO,
                successRate = if (orders.isNotEmpty()) (orders.count { it.status == OrderStatus.COMPLETED }.toDouble() / orders.size * 100) else 0.0,
                completionRate = if (orders.isNotEmpty()) (orders.count { it.status in listOf(OrderStatus.COMPLETED, OrderStatus.IN_PROGRESS) }.toDouble() / orders.size * 100) else 0.0
            ),
            statusSummary = StatusSummary(
                pendingOrders = orders.count { it.status == OrderStatus.PENDING },
                paymentWaitingOrders = orders.count { it.status == OrderStatus.PAYMENT_WAITING },
                paymentConfirmedOrders = orders.count { it.status == OrderStatus.PAYMENT_CONFIRMED },
                approvedOrders = orders.count { it.status == OrderStatus.APPROVED },
                inProgressOrders = orders.count { it.status == OrderStatus.IN_PROGRESS },
                completedOrders = orders.count { it.status == OrderStatus.COMPLETED },
                failedOrders = orders.count { it.status == OrderStatus.FAILED },
                cancelledOrders = orders.count { it.status == OrderStatus.CANCELLED }
            ),
            dailyTrend = dailyStats.map {
                DailyTrend(
                    date = it["date"] as java.time.LocalDate,
                    newOrders = it["newOrders"] as Long,
                    completedOrders = it["completedOrders"] as Long
                )
            },
            topPerformers = getTopPerformers(orders)
        )
    }

    private fun getPeriodDates(period: String): Pair<LocalDateTime, LocalDateTime> {
        val now = LocalDateTime.now()
        return when (period) {
            "thisMonth" -> {
                val startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                val endOfMonth = startOfMonth.plusMonths(1)
                startOfMonth to endOfMonth
            }
            "last3Months" -> {
                val start = now.minusMonths(3).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                val end = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                start to end
            }
            "thisYear" -> {
                val startOfYear = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                val endOfYear = startOfYear.plusYears(1)
                startOfYear to endOfYear
            }
            "all" -> {
                val start = LocalDateTime.of(2020, 1, 1, 0, 0)
                val end = now.plusDays(1)
                start to end
            }
            else -> {
                val startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                val endOfMonth = startOfMonth.plusMonths(1)
                startOfMonth to endOfMonth
            }
        }
    }

    private fun calculateTotalAmount(orders: List<Order>): BigDecimal {
        return orders.mapNotNull { order ->
            orderPaymentRepository.findByOrderIdOrderByCreatedAtDesc(order.id!!)
                .firstOrNull()?.paymentAmount
        }.fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount) }
    }

    private fun getTopPerformers(orders: List<Order>): List<TopPerformer> {
        return orders.groupBy { it.member }
            .mapValues { (member, memberOrders) ->
                TopPerformer(
                    memberId = member.id!!,
                    memberCompanyName = member.companyName ?: "",
                    totalOrders = memberOrders.size,
                    completedOrders = memberOrders.count { it.status == OrderStatus.COMPLETED },
                    totalAmount = calculateTotalAmount(memberOrders)
                )
            }
            .values
            .sortedByDescending { it.totalAmount }
            .take(10)
    }

    @Transactional
    fun createTestOrderWithRound(roundId: Int, request: OrderCreateRequest): List<OrderResponse> {
        val round = roundRepository.findById(roundId.toLong()).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")
        
        val adTasks = adTaskRepository.findByRoundAndAdIndex(round, 1)
        if (adTasks.isEmpty()) {
            throw IllegalArgumentException("No AdTask with ad_index=1 found for round: $roundId")
        }

        val orders = adTasks.map { adTask ->
            val orderNumber = generateOrderNumber()
            
            val order = Order(
                orderNumber = orderNumber,
                adTask = adTask,
                member = adTask.member,
                productName = request.productName,
                quantity = request.quantity,
                requirements = request.requirements,
                deadline = request.deadline,
                status = OrderStatus.PENDING
            )
            
            orderRepository.save(order)
        }

        return orders.map { mapToOrderResponse(it) }
    }

    @Transactional
    fun createTestPaymentWithRound(roundId: Int, request: TestPaymentCreateRequest): List<OrderPaymentInfo> {
        val round = roundRepository.findById(roundId.toLong()).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")
        
        // 해당 라운드의 모든 Order 조회 (AdTask를 통하지 않고 직접 조회)
        val orders = orderRepository.findByRoundId(roundId.toLong())
        if (orders.isEmpty()) {
            throw IllegalArgumentException("No orders found for round: $roundId")
        }

        val payments = orders.mapNotNull { order ->
            // 이미 결제가 있는 주문은 건너뛰기
            val existingPayment = orderPaymentRepository.findByOrderId(order.id!!)
            if (existingPayment != null) {
                logger.info("Order ${order.id} already has payment (${existingPayment.applicationNumber}). Skipping.")
                return@mapNotNull null
            }
            
            if (order.status != OrderStatus.PENDING) {
                logger.warn("Order ${order.id} is not in PENDING status. Current status: ${order.status}. Skipping.")
                return@mapNotNull null
            }

            val applicationNumber = generateApplicationNumber()
            
            val payment = OrderPayment(
                order = order,
                applicationNumber = applicationNumber,
                paymentAmount = request.paymentAmount,
                depositorName = request.depositorName,
                bankAccountNumber = request.bankAccountNumber,
                bankName = request.bankName,
                notes = request.notes,
                paymentStatus = PaymentStatus.WAITING
            )

            val savedPayment = orderPaymentRepository.save(payment)
            updateOrderStatus(order.id!!, OrderStatusUpdateRequest(OrderStatus.PAYMENT_WAITING))
            
            mapToOrderPaymentInfo(savedPayment)
        }

        if (payments.isEmpty()) {
            val totalOrders = orders.size
            val ordersWithPayments = orders.count { order ->
                orderPaymentRepository.findByOrderId(order.id!!) != null
            }
            val pendingOrders = orders.count { it.status == OrderStatus.PENDING }
            
            logger.warn("No new payments created for round $roundId. Total orders: $totalOrders, Orders with existing payments: $ordersWithPayments, Pending orders: $pendingOrders")
            throw IllegalStateException("No valid orders found to create payments for round: $roundId. All orders either already have payments or are not in PENDING status.")
        }

        logger.info("Created ${payments.size} payments for round $roundId")
        return payments
    }

    @Transactional
    fun confirmAllPaymentsByRound(roundId: Int, notes: String = "라운드별 일괄 입금 확인"): List<OrderPaymentInfo> {
        roundRepository.findById(roundId.toLong()).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")
        
        // 조인 쿼리로 업데이트 대상 조회
        val paymentsToUpdate = orderPaymentRepository.findPaymentsToConfirmByRound(roundId.toLong())
        
        if (paymentsToUpdate.isEmpty()) {
            throw IllegalArgumentException("No payments in WAITING status found for round: $roundId with ad_index=1")
        }

        val confirmedPayments = paymentsToUpdate.map { payment ->
            val updatedPayment = OrderPayment(
                id = payment.id,
                order = payment.order,
                applicationNumber = payment.applicationNumber,
                paymentAmount = payment.paymentAmount,
                depositorName = payment.depositorName,
                bankAccountNumber = payment.bankAccountNumber,
                bankName = payment.bankName,
                paymentStatus = PaymentStatus.CONFIRMED,
                notes = notes,
                paymentConfirmedAt = LocalDateTime.now(),
                createdAt = payment.createdAt,
                updatedAt = LocalDateTime.now()
            )

            val savedPayment = orderPaymentRepository.save(updatedPayment)
            
            updateOrderStatus(payment.order.id!!, OrderStatusUpdateRequest(OrderStatus.PAYMENT_CONFIRMED))
            
            // 입금 확인 시 expense transaction 생성
            try {
                revenueService.createExpenseTransaction(payment.order)
            } catch (e: Exception) {
                logger.warn("Failed to create expense transaction for order ${payment.order.id}: ${e.message}")
            }
            
            mapToOrderPaymentInfo(savedPayment)
        }

        return confirmedPayments
    }

    @Transactional
    fun confirmPaymentsByRoundAndMember(roundId: Int, memberId: Long, notes: String = "멤버별 입금 확인"): List<OrderPaymentInfo> {
        roundRepository.findById(roundId.toLong()).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")
        
        memberRepository.findById(memberId).orElse(null)
            ?: throw IllegalArgumentException("Member not found with id: $memberId")
        
        // 조인 쿼리로 특정 멤버의 업데이트 대상 조회
        val paymentsToUpdate = orderPaymentRepository.findPaymentsToConfirmByRoundAndMember(roundId.toLong(),  memberId)
        
        if (paymentsToUpdate.isEmpty()) {
            throw IllegalArgumentException("No payments in WAITING status found for round: $roundId, member: $memberId with ad_index=1")
        }

        val confirmedPayments = paymentsToUpdate.map { payment ->
            val updatedPayment = OrderPayment(
                id = payment.id,
                order = payment.order,
                applicationNumber = payment.applicationNumber,
                paymentAmount = payment.paymentAmount,
                depositorName = payment.depositorName,
                bankAccountNumber = payment.bankAccountNumber,
                bankName = payment.bankName,
                paymentStatus = PaymentStatus.CONFIRMED,
                notes = notes,
                paymentConfirmedAt = LocalDateTime.now(),
                createdAt = payment.createdAt,
                updatedAt = LocalDateTime.now()
            )

            val savedPayment = orderPaymentRepository.save(updatedPayment)
            
            updateOrderStatus(payment.order.id!!, OrderStatusUpdateRequest(OrderStatus.PAYMENT_CONFIRMED))
            
            // 입금 확인 시 expense transaction 생성
            try {
                revenueService.createExpenseTransaction(payment.order)
            } catch (e: Exception) {
                logger.warn("Failed to create expense transaction for order ${payment.order.id}: ${e.message}")
            }
            
            mapToOrderPaymentInfo(savedPayment)
        }

        return confirmedPayments
    }

    fun getRoundInfo(roundId: Int): RoundInfoResponse {
        val round = roundRepository.findById(roundId.toLong()).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")
        
        val adTasks = adTaskRepository.findByRoundAndAdIndex(round, 1)
        
        val adTaskInfos = adTasks.map { adTask ->
            AdTaskInfo(
                id = adTask.id!!,
                memberId = adTask.member.id!!,
                memberEmail = adTask.member.email,
                memberCompanyName = adTask.member.companyName,
                adType = adTask.adType,
                adIndex = adTask.adIndex,
                taskStatus = adTask.status.name,
                webUrl = adTask.webUrl,
                createdAt = adTask.createdAt
            )
        }
        
        val orders = adTasks.mapNotNull { adTask ->
            orderRepository.findByAdTaskId(adTask.id!!)
        }
        
        val orderInfos = orders.map { order ->
            OrderInfo(
                id = order.id!!,
                orderNumber = order.orderNumber,
                adTaskId = order.adTask.id!!,
                memberId = order.member.id!!,
                memberEmail = order.member.email,
                memberCompanyName = order.member.companyName,
                productName = order.productName,
                quantity = order.quantity,
                status = order.status.name,
                submittedAt = order.submittedAt,
                createdAt = order.createdAt
            )
        }
        
        val payments = orders.flatMap { order ->
            orderPaymentRepository.findByOrderIdOrderByCreatedAtDesc(order.id!!)
        }
        
        val paymentInfos = payments.map { payment ->
            RoundPaymentInfo(
                id = payment.id!!,
                orderId = payment.order.id!!,
                applicationNumber = payment.applicationNumber,
                paymentAmount = payment.paymentAmount,
                depositorName = payment.depositorName,
                paymentStatus = payment.paymentStatus.name,
                bankName = payment.bankName,
                bankAccountNumber = payment.bankAccountNumber,
                paymentConfirmedAt = payment.paymentConfirmedAt,
                createdAt = payment.createdAt
            )
        }
        
        return RoundInfoResponse(
            roundId = roundId.toLong(),
            roundTitle = round.title ?: "Round #$roundId",
            adTasks = adTaskInfos,
            orders = orderInfos,
            payments = paymentInfos
        )
    }

    fun getRoundKeys(roundId: Int): RoundKeysResponse {
        val round = roundRepository.findById(roundId.toLong()).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")
        
        val adTasks = adTaskRepository.findByRoundAndAdIndex(round, 1)
        
        val memberKeys = adTasks.map { adTask ->
            val order = orderRepository.findByAdTaskId(adTask.id!!)
            val payments = order?.let { 
                orderPaymentRepository.findByOrderIdOrderByCreatedAtDesc(it.id!!)
            } ?: emptyList()
            
            MemberKeys(
                memberId = adTask.member.id!!,
                memberEmail = adTask.member.email,
                memberCompanyName = adTask.member.companyName,
                adTaskId = adTask.id,
                orderId = order?.id,
                paymentIds = payments.mapNotNull { it.id }
            )
        }
        
        return RoundKeysResponse(
            roundId = roundId.toLong(),
            roundTitle = round.title ?: "Round #$roundId",
            members = memberKeys
        )
    }

    @Transactional
    fun startAdvertisingForRound(roundId: Long, notes: String): List<OrderResponse> {
        val round = roundRepository.findById(roundId).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")
        
        // 라운드의 모든 AdTask 조회
        val adTasks = adTaskRepository.findByRoundId(roundId)
        if (adTasks.isEmpty()) {
            logger.info("라운드 $roundId 에 AdTask가 없습니다.")
            return emptyList()
        }
        
        // 라운드의 모든 주문 조회 (PAYMENT_CONFIRMED 상태인 것만)
        val paymentConfirmedOrders = adTasks.mapNotNull { adTask ->
            orderRepository.findByAdTaskId(adTask.id!!)
                ?.takeIf { it.status == OrderStatus.PAYMENT_CONFIRMED }
        }
        
        if (paymentConfirmedOrders.isEmpty()) {
            logger.info("라운드 $roundId 에 입금 확인된 주문이 없습니다.")
            return emptyList()
        }
        
        // 모든 입금 확인된 주문들을 IN_PROGRESS로 업데이트
        val startedOrders = paymentConfirmedOrders.map { order ->
            val updatedOrder = order.updateStatus(
                newStatus = OrderStatus.IN_PROGRESS,
                notes = notes,
                progressRate = 0 // 광고 게시 시작 시 진행률 0%로 초기화
            )
            orderRepository.save(updatedOrder)
        }
        
        logger.info("라운드 $roundId 의 ${startedOrders.size}개 주문이 광고 게시중 상태로 변경되었습니다.")
        
        // 라운드 상태를 CLOSED로 업데이트
        round.updatedAt = LocalDateTime.now()
        val updatedRound = round.copy(status = com.service.frame.round.entity.RoundStatus.CLOSED)
        roundRepository.save(updatedRound)
        logger.info("라운드 $roundId 의 상태가 CLOSED로 변경되었습니다.")
        
        return startedOrders.map { order ->
            val member = memberRepository.findById(order.member.id!!).orElse(null)
            val adTask = adTaskRepository.findById(order.adTask.id!!).orElse(null)
            val payment = order.getCurrentPayment()
            val paymentInfo = payment?.let {
                OrderPaymentInfo(
                    id = it.id!!,
                    applicationNumber = it.applicationNumber,
                    paymentAmount = it.paymentAmount,
                    depositorName = it.depositorName,
                    paymentStatus = it.paymentStatus,
                    bankAccountNumber = it.bankAccountNumber,
                    bankName = it.bankName,
                    paymentConfirmedAt = it.paymentConfirmedAt,
                    notes = it.notes
                )
            }
            
            // Round 정보 생성
            val roundInfo = RoundInfo(
                id = order.adTask.round.id!!,
                title = order.adTask.round.title,
                description = order.adTask.round.description,
                category = order.adTask.round.category,
                orderAmount = order.adTask.round.orderAmount,
                status = order.adTask.round.status,
                startDate = order.adTask.round.startDate?.toLocalDate(),
                endDate = order.adTask.round.endDate?.toLocalDate(),
                postStartDate = order.adTask.round.postStartDate?.toLocalDate(),
                postEndDate = order.adTask.round.postEndDate?.toLocalDate(),
                postDurationDays = order.adTask.round.postDurationDays,
                maxParticipants = order.adTask.round.maxParticipants,
                currentParticipants = orderRepository.countByRoundId(order.adTask.round.id!!).toInt(),
                roundNumber = order.adTask.round.roundNumber
            )

            OrderResponse(
                id = order.id!!,
                orderNumber = order.orderNumber,
                memberId = order.member.id!!,
                memberEmail = member?.email ?: "",
                memberCompanyName = member?.companyName ?: "",
                adTaskId = order.adTask.id!!,
                adTaskTitle = adTask?.webUrl ?: "",
                productName = order.productName,
                quantity = order.quantity,
                requirements = order.requirements,
                deadline = order.deadline,
                startDate = order.startDate,
                completionDate = order.completionDate,
                failureDate = order.failureDate,
                progressRate = order.progressRate,
                failureReason = order.failureReason,
                status = order.status,
                submittedAt = order.submittedAt,
                reviewedAt = order.reviewedAt,
                reviewedByName = order.reviewedBy?.companyName,
                notes = order.notes,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
                paymentInfo = paymentInfo,
                roundParticipants = null,
                roundInfo = roundInfo
            )
        }
    }

    @Transactional
    fun completeOrdersIfAllPostsPublished(roundId: Long, notes: String): List<OrderResponse> {
        val round = roundRepository.findById(roundId).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")
        
        // 라운드의 모든 AdTask 조회
        val adTasks = adTaskRepository.findByRoundId(roundId)
        if (adTasks.isEmpty()) {
            logger.info("라운드 $roundId 에 AdTask가 없습니다.")
            return emptyList()
        }
        
        // 라운드의 모든 주문 조회 (IN_PROGRESS 상태인 것만)
        val inProgressOrders = adTasks.mapNotNull { adTask ->
            orderRepository.findByAdTaskId(adTask.id!!)
                ?.takeIf { it.status == OrderStatus.IN_PROGRESS }
        }
        
        if (inProgressOrders.isEmpty()) {
            logger.info("라운드 $roundId 에 진행중인 주문이 없습니다.")
            return emptyList()
        }
        
        // 라운드의 모든 포스트가 PUBLISHED 상태인지 확인
        val allPostsPublished = checkAllPostsPublished(roundId)
        
        if (!allPostsPublished) {
            logger.info("라운드 $roundId 의 모든 포스트가 아직 게시되지 않았습니다.")
            return emptyList()
        }
        
        // 모든 포스트가 게시되었다면 주문들을 COMPLETED로 업데이트
        val completedOrders = inProgressOrders.map { order ->
            val updatedOrder = order.updateStatus(
                newStatus = OrderStatus.COMPLETED,
                notes = notes,
                progressRate = 100
            )
            val savedOrder = orderRepository.save(updatedOrder)
            
            // revenue_transactions 테이블의 order_status도 업데이트
            try {
                revenueService.updateOrderStatusInRevenueTransactions(savedOrder)
            } catch (e: Exception) {
                logger.warn("Failed to update revenue transaction order status for order ${savedOrder.id}: ${e.message}")
            }
            
            savedOrder
        }
        
        logger.info("라운드 $roundId 의 ${completedOrders.size}개 주문이 완료 상태로 업데이트되었습니다.")
        
        // 모든 주문이 완료되었으므로 라운드 상태를 CLOSED로 변경
        // Round 엔티티의 mutable 필드를 업데이트하고 저장
        round.updatedAt = LocalDateTime.now()
        val updatedRound = round.copy(status = com.service.frame.round.entity.RoundStatus.CLOSED)
        roundRepository.save(updatedRound)
        logger.info("라운드 $roundId 의 상태가 CLOSED로 변경되었습니다.")
        
        return completedOrders.map { order ->
            val member = memberRepository.findById(order.member.id!!).orElse(null)
            val adTask = adTaskRepository.findById(order.adTask.id!!).orElse(null)
            val payment = order.getCurrentPayment()
            val paymentInfo = payment?.let {
                OrderPaymentInfo(
                    id = it.id!!,
                    applicationNumber = it.applicationNumber,
                    paymentAmount = it.paymentAmount,
                    depositorName = it.depositorName,
                    paymentStatus = it.paymentStatus,
                    bankAccountNumber = it.bankAccountNumber,
                    bankName = it.bankName,
                    paymentConfirmedAt = it.paymentConfirmedAt,
                    notes = it.notes
                )
            }
            
            // Round 정보 생성
            val roundInfo = RoundInfo(
                id = order.adTask.round.id!!,
                title = order.adTask.round.title,
                description = order.adTask.round.description,
                category = order.adTask.round.category,
                orderAmount = order.adTask.round.orderAmount,
                status = order.adTask.round.status,
                startDate = order.adTask.round.startDate?.toLocalDate(),
                endDate = order.adTask.round.endDate?.toLocalDate(),
                postStartDate = order.adTask.round.postStartDate?.toLocalDate(),
                postEndDate = order.adTask.round.postEndDate?.toLocalDate(),
                postDurationDays = order.adTask.round.postDurationDays,
                maxParticipants = order.adTask.round.maxParticipants,
                currentParticipants = orderRepository.countByRoundId(order.adTask.round.id!!).toInt(),
                roundNumber = order.adTask.round.roundNumber
            )

            OrderResponse(
                id = order.id!!,
                orderNumber = order.orderNumber,
                memberId = order.member.id!!,
                memberEmail = member?.email ?: "",
                memberCompanyName = member?.companyName ?: "",
                adTaskId = order.adTask.id!!,
                adTaskTitle = adTask?.webUrl ?: "",
                productName = order.productName,
                quantity = order.quantity,
                requirements = order.requirements,
                deadline = order.deadline,
                startDate = order.startDate,
                completionDate = order.completionDate,
                failureDate = order.failureDate,
                progressRate = order.progressRate,
                failureReason = order.failureReason,
                status = order.status,
                submittedAt = order.submittedAt,
                reviewedAt = order.reviewedAt,
                reviewedByName = order.reviewedBy?.companyName,
                notes = order.notes,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
                paymentInfo = paymentInfo,
                roundParticipants = null,
                roundInfo = roundInfo
            )
        }
    }
    
    @Transactional
    fun updateOrderProgressByRound(roundId: Long): List<OrderResponse> {
        val round = roundRepository.findById(roundId).orElse(null)
            ?: throw IllegalArgumentException("Round not found with id: $roundId")
        
        // 라운드의 모든 AdTask 조회
        val adTasks = adTaskRepository.findByRoundId(roundId)
        if (adTasks.isEmpty()) {
            logger.info("라운드 $roundId 에 AdTask가 없습니다.")
            return emptyList()
        }
        
        val updatedOrders = mutableListOf<Order>()
        
        adTasks.forEach { adTask ->
            val order = orderRepository.findByAdTaskId(adTask.id!!)
            if (order != null && order.status == OrderStatus.IN_PROGRESS) {
                // 해당 AdTask의 포스트 진행률 계산
                val progressRate = calculatePostProgressForAdTask(adTask.id!!)
                
                val updatedOrder = order.copy(
                    progressRate = progressRate,
                    updatedAt = LocalDateTime.now()
                )
                
                val savedOrder = orderRepository.save(updatedOrder)
                updatedOrders.add(savedOrder)
                
                logger.info("AdTask ${adTask.id}의 주문 진행률 업데이트: $progressRate%")
            }
        }
        
        logger.info("라운드 $roundId 의 ${updatedOrders.size}개 주문 진행률이 업데이트되었습니다.")
        
        return updatedOrders.map { order ->
            val member = memberRepository.findById(order.member.id!!).orElse(null)
            val adTask = adTaskRepository.findById(order.adTask.id!!).orElse(null)
            val payment = order.getCurrentPayment()
            val paymentInfo = payment?.let {
                OrderPaymentInfo(
                    id = it.id!!,
                    applicationNumber = it.applicationNumber,
                    paymentAmount = it.paymentAmount,
                    depositorName = it.depositorName,
                    paymentStatus = it.paymentStatus,
                    bankAccountNumber = it.bankAccountNumber,
                    bankName = it.bankName,
                    paymentConfirmedAt = it.paymentConfirmedAt,
                    notes = it.notes
                )
            }
            
            // Round 정보 생성
            val roundInfo = RoundInfo(
                id = order.adTask.round.id!!,
                title = order.adTask.round.title,
                description = order.adTask.round.description,
                category = order.adTask.round.category,
                orderAmount = order.adTask.round.orderAmount,
                status = order.adTask.round.status,
                startDate = order.adTask.round.startDate?.toLocalDate(),
                endDate = order.adTask.round.endDate?.toLocalDate(),
                postStartDate = order.adTask.round.postStartDate?.toLocalDate(),
                postEndDate = order.adTask.round.postEndDate?.toLocalDate(),
                postDurationDays = order.adTask.round.postDurationDays,
                maxParticipants = order.adTask.round.maxParticipants,
                currentParticipants = orderRepository.countByRoundId(order.adTask.round.id!!).toInt(),
                roundNumber = order.adTask.round.roundNumber
            )

            OrderResponse(
                id = order.id!!,
                orderNumber = order.orderNumber,
                memberId = order.member.id!!,
                memberEmail = member?.email ?: "",
                memberCompanyName = member?.companyName ?: "",
                adTaskId = order.adTask.id!!,
                adTaskTitle = adTask?.webUrl ?: "",
                productName = order.productName,
                quantity = order.quantity,
                requirements = order.requirements,
                deadline = order.deadline,
                startDate = order.startDate,
                completionDate = order.completionDate,
                failureDate = order.failureDate,
                progressRate = order.progressRate,
                failureReason = order.failureReason,
                status = order.status,
                submittedAt = order.submittedAt,
                reviewedAt = order.reviewedAt,
                reviewedByName = order.reviewedBy?.companyName,
                notes = order.notes,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
                paymentInfo = paymentInfo,
                roundParticipants = null,
                roundInfo = roundInfo
            )
        }
    }
    
    private fun calculatePostProgressForAdTask(adTaskId: Long): Int {
        return try {
            val progressRate = orderRepository.calculatePostProgressForAdTask(adTaskId)
            progressRate.coerceIn(0, 100) // 0-100% 범위로 제한
        } catch (e: Exception) {
            logger.error("AdTask $adTaskId 의 포스트 진행률 계산 중 오류 발생", e)
            0
        }
    }
    
    private fun checkAllPostsPublished(roundId: Long): Boolean {
        // AdvertisementAssignmentRepository를 통해 라운드의 모든 할당 조회
        // 각 할당에 대응하는 포스트가 모두 PUBLISHED 상태인지 확인
        return try {
            // HTTP 호출 대신 직접 데이터베이스 쿼리로 확인
            // 이는 같은 애플리케이션 내의 다른 서비스를 호출하는 것이므로 
            // 직접 repository를 주입받아 사용하는 것이 더 효율적
            val result = orderRepository.checkAllPostsPublishedInRound(roundId)
            logger.info("라운드 $roundId 포스트 게시 상태 확인 결과: $result")
            result
        } catch (e: Exception) {
            logger.error("라운드 $roundId 포스트 게시 상태 확인 중 오류 발생", e)
            false
        }
    }

    @Transactional
    fun purchaseAd(request: AdPurchaseRequest): AdPurchaseResponse {
        val member = memberRepository.findById(request.memberId).orElse(null)
            ?: throw IllegalArgumentException("Member not found with id: ${request.memberId}")
        
        val adTask = adTaskRepository.findById(request.adTaskId).orElse(null)
            ?: throw IllegalArgumentException("AdTask not found with id: ${request.adTaskId}")

        val round = adTask.round
        val orderNumber = generateOrderNumber()
        
        // 자동으로 productName 생성: 라운드명 + 광고타입 + 광고번호
        val productName = "${round.title} ${adTask.adType ?: "광고"} ${adTask.adIndex ?: 1}번"
        
        // 자동으로 deadline 설정: round의 post_end_date
        val deadline = round.getCalculatedPostEndDate().toLocalDate()
        
        val order = Order(
            orderNumber = orderNumber,
            adTask = adTask,
            member = member,
            productName = productName,
            quantity = request.quantity,
            requirements = request.requirements,
            deadline = deadline,
            startDate = round.postStartDate?.toLocalDate(),
            completionDate = round.postEndDate?.toLocalDate(),
            status = OrderStatus.PENDING
        )

        val savedOrder = orderRepository.save(order)

        val applicationNumber = generateApplicationNumber()
        val paymentAmount = round.orderAmount
        
        // 자동으로 depositorName 설정: member의 회사명 또는 이메일
        val depositorName = member.companyName ?: member.email
        
        val payment = OrderPayment(
            order = savedOrder,
            applicationNumber = applicationNumber,
            paymentAmount = paymentAmount,
            depositorName = depositorName,
            bankAccountNumber = "110-123-456789",
            bankName = "신한은행",
            notes = "로그인한 유저가 광고 구매 - 입금 대기",
            paymentStatus = PaymentStatus.WAITING
        )

        val savedPayment = orderPaymentRepository.save(payment)
        
        updateOrderStatus(savedOrder.id!!, OrderStatusUpdateRequest(OrderStatus.PAYMENT_WAITING))

        return AdPurchaseResponse(
            order = mapToOrderResponse(savedOrder),
            payment = mapToOrderPaymentInfo(savedPayment)
        )
    }

    fun getMyPurchases(memberId: Long): List<MyPurchaseResponse> {
        val member = memberRepository.findById(memberId).orElse(null)
            ?: throw IllegalArgumentException("Member not found with id: $memberId")

        val orders = orderRepository.findByMemberIdWithDetailsOrderBySubmittedAtDesc(memberId)
        
        return orders.map { order ->
            val latestPayment = orderPaymentRepository.findByOrderIdOrderByCreatedAtDesc(order.id!!)
                .firstOrNull()
            
            val roundId = order.adTask.round.id!!
            val totalParticipants = orderRepository.countByRoundId(roundId)
            
            // 광고 URL 조회 (advertisement_assignments 테이블에서)
            val adUrl = getAdUrlForOrder(order.id!!)
            
            // 영수증 정보 계산
            val unitPrice = order.adTask.round.orderAmount
            val subtotal = unitPrice.multiply(BigDecimal(order.quantity))
            val vatAmount = subtotal.multiply(BigDecimal("0.1"))
            val finalAmount = subtotal.add(vatAmount)
            
            val receiptNumber = "REC-${order.orderNumber?.replace("ORD-", "") ?: order.id}"
            
            MyPurchaseResponse(
                orderId = order.id!!,
                orderNumber = order.orderNumber ?: "",
                roundId = roundId,
                adTaskId = order.adTask.id!!,
                productName = order.productName,
                quantity = order.quantity,
                orderStatus = order.status,
                orderDate = order.submittedAt,
                paymentStatus = latestPayment?.paymentStatus,
                paymentAmount = order.adTask.round.orderAmount,
                deadline = order.deadline,
                requirements = order.requirements,
                totalParticipants = totalParticipants.toInt(),
                roundTitle = "Round #${roundId}",
                adUrl = adUrl,
                receiptInfo = ReceiptSummary(
                    receiptNumber = receiptNumber,
                    totalAmount = subtotal,
                    vatAmount = vatAmount,
                    finalAmount = finalAmount
                )
            )
        }
    }

    fun downloadAdUrl(orderId: Long): ByteArray {
        val order = orderRepository.findById(orderId).orElse(null)
            ?: throw IllegalArgumentException("Order not found with id: $orderId")
        
        val adUrl = getAdUrlForOrder(orderId)
            ?: throw IllegalArgumentException("Ad URL not found for order: $orderId")
        
        // URL 내용을 가져와서 octet-stream으로 반환
        return try {
            java.net.URL(adUrl).readBytes()
        } catch (e: Exception) {
            // URL에서 직접 읽을 수 없는 경우 URL 자체를 반환
            adUrl.toByteArray(Charsets.UTF_8)
        }
    }
    
    private fun getAdUrlForOrder(orderId: Long): String? {
        // TODO: advertisement_assignments 테이블에서 광고 URL 조회
        // 현재는 임시로 null 반환
        return null
    }

    fun getOrderReceipt(orderId: Long): OrderReceiptResponse {
        val order = orderRepository.findById(orderId).orElse(null)
            ?: throw IllegalArgumentException("Order not found with id: $orderId")

        val latestPayment = orderPaymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
            .firstOrNull()

        val receiptNumber = "REC-${order.orderNumber?.replace("ORD-", "") ?: orderId}"
        val unitPrice = order.adTask.round.orderAmount
        val subtotal = unitPrice.multiply(BigDecimal(order.quantity))
        val vatAmount = subtotal.multiply(BigDecimal("0.1")) // 10% VAT
        val finalAmount = subtotal.add(vatAmount)

        return OrderReceiptResponse(
            receiptNumber = receiptNumber,
            orderNumber = order.orderNumber ?: "ORD-$orderId",
            issueDate = LocalDateTime.now(),
            customerInfo = CustomerInfo(
                companyName = order.member.companyName ?: "개인",
                email = order.member.email,
                contactNumber = order.member.contactNumber,
                businessRegistrationNumber = order.member.businessRegistrationNumber
            ),
            orderDetails = OrderDetails(
                productName = order.productName,
                quantity = order.quantity,
                unitPrice = unitPrice,
                subtotal = subtotal,
                orderDate = order.submittedAt,
                deadline = order.deadline,
                requirements = order.requirements,
                roundId = order.adTask.round.id!!,
                adTaskId = order.adTask.id!!
            ),
            paymentInfo = PaymentInfo(
                paymentMethod = "무통장입금",
                paymentStatus = latestPayment?.paymentStatus ?: PaymentStatus.WAITING,
                paymentDate = latestPayment?.paymentConfirmedAt,
                depositorName = latestPayment?.depositorName,
                bankName = latestPayment?.bankName ?: "신한은행",
                accountNumber = latestPayment?.bankAccountNumber ?: "110-123-456789",
                applicationNumber = latestPayment?.applicationNumber
            ),
            companyInfo = CompanyInfo(),
            totalAmount = subtotal,
            vatAmount = vatAmount,
            finalAmount = finalAmount
        )
    }

    fun downloadOrderReceipt(orderId: Long): ByteArray {
        val receipt = getOrderReceipt(orderId)
        
        // HTML 기반 영수증 생성
        val htmlContent = generateReceiptHtml(receipt)
        
        // TODO: 실제로는 HTML을 PDF로 변환하는 라이브러리 사용
        // 현재는 HTML을 바이트 배열로 반환
        return htmlContent.toByteArray(Charsets.UTF_8)
    }

    fun getMemberReceipts(memberId: Long): List<OrderReceiptSummary> {
        val member = memberRepository.findById(memberId).orElse(null)
            ?: throw IllegalArgumentException("Member not found with id: $memberId")

        val orders = orderRepository.findByMemberIdWithDetailsOrderBySubmittedAtDesc(memberId)
        
        return orders.map { order ->
            val latestPayment = orderPaymentRepository.findByOrderIdOrderByCreatedAtDesc(order.id!!)
                .firstOrNull()
            
            val receiptNumber = "REC-${order.orderNumber?.replace("ORD-", "") ?: order.id}"
            val unitPrice = order.adTask.round.orderAmount
            val subtotal = unitPrice.multiply(BigDecimal(order.quantity))
            val vatAmount = subtotal.multiply(BigDecimal("0.1"))
            val finalAmount = subtotal.add(vatAmount)

            OrderReceiptSummary(
                orderId = order.id!!,
                receiptNumber = receiptNumber,
                orderNumber = order.orderNumber ?: "ORD-${order.id}",
                productName = order.productName,
                totalAmount = finalAmount,
                paymentStatus = latestPayment?.paymentStatus ?: PaymentStatus.WAITING,
                issueDate = LocalDateTime.now(),
                orderDate = order.submittedAt
            )
        }
    }

    private fun generateReceiptHtml(receipt: OrderReceiptResponse): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>영수증 - ${receipt.receiptNumber}</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                .header { text-align: center; margin-bottom: 30px; }
                .company-info { margin-bottom: 20px; }
                .customer-info { margin-bottom: 20px; }
                .order-details { margin-bottom: 20px; }
                .payment-info { margin-bottom: 20px; }
                .total-section { border-top: 2px solid #000; padding-top: 10px; font-weight: bold; }
                table { width: 100%; border-collapse: collapse; }
                th, td { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>영수증</h1>
                <h2>${receipt.receiptNumber}</h2>
                <p>발행일: ${receipt.issueDate}</p>
            </div>

            <div class="company-info">
                <h3>발행업체</h3>
                <p>${receipt.companyInfo.companyName}</p>
                <p>사업자번호: ${receipt.companyInfo.businessNumber}</p>
                <p>주소: ${receipt.companyInfo.address}</p>
                <p>전화: ${receipt.companyInfo.phone}</p>
            </div>

            <div class="customer-info">
                <h3>고객정보</h3>
                <p>회사명: ${receipt.customerInfo.companyName}</p>
                <p>이메일: ${receipt.customerInfo.email}</p>
                <p>연락처: ${receipt.customerInfo.contactNumber ?: "없음"}</p>
                <p>사업자번호: ${receipt.customerInfo.businessRegistrationNumber ?: "없음"}</p>
            </div>

            <div class="order-details">
                <h3>주문정보</h3>
                <table>
                    <tr><th>주문번호</th><td>${receipt.orderNumber}</td></tr>
                    <tr><th>상품명</th><td>${receipt.orderDetails.productName}</td></tr>
                    <tr><th>수량</th><td>${receipt.orderDetails.quantity}</td></tr>
                    <tr><th>단가</th><td>${receipt.orderDetails.unitPrice.toPlainString()}원</td></tr>
                    <tr><th>소계</th><td>${receipt.orderDetails.subtotal.toPlainString()}원</td></tr>
                    <tr><th>주문일</th><td>${receipt.orderDetails.orderDate}</td></tr>
                    <tr><th>마감일</th><td>${receipt.orderDetails.deadline ?: "없음"}</td></tr>
                </table>
            </div>

            <div class="payment-info">
                <h3>결제정보</h3>
                <p>결제방법: ${receipt.paymentInfo.paymentMethod}</p>
                <p>결제상태: ${receipt.paymentInfo.paymentStatus}</p>
                <p>은행: ${receipt.paymentInfo.bankName}</p>
                <p>계좌번호: ${receipt.paymentInfo.accountNumber}</p>
                <p>입금자: ${receipt.paymentInfo.depositorName ?: "미확인"}</p>
            </div>

            <div class="total-section">
                <table>
                    <tr><th>합계</th><td>${receipt.totalAmount.toPlainString()}원</td></tr>
                    <tr><th>부가세(10%)</th><td>${receipt.vatAmount.toPlainString()}원</td></tr>
                    <tr><th>총액</th><td>${receipt.finalAmount.toPlainString()}원</td></tr>
                </table>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}