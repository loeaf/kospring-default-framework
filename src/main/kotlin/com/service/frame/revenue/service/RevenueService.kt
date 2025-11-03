package com.service.frame.revenue.service

import com.service.frame.revenue.dto.*
import com.service.frame.revenue.entity.RevenueTransaction
import com.service.frame.revenue.entity.TransactionType
import com.service.frame.revenue.repository.RevenueTransactionRepository
import com.service.frame.post.repository.AdvertisementPostRepository
import com.service.frame.post.entity.AdvertisementPost
import com.service.frame.order.repository.OrderRepository
import com.service.frame.order.entity.Order
import com.service.frame.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class RevenueService(
    private val revenueTransactionRepository: RevenueTransactionRepository,
    private val postRepository: AdvertisementPostRepository,
    private val orderRepository: OrderRepository,
    private val memberRepository: MemberRepository
) {
    
    fun getRevenueList(request: RevenueFilterRequest, memberId: Long): RevenueListResponse {
        // fetch join을 사용하여 N+1 문제 해결
        val transactions = if (request.transactionType != null) {
            revenueTransactionRepository.findByMemberIdAndTransactionTypeWithFetch(
                memberId, 
                request.transactionType!!
            )
        } else {
            revenueTransactionRepository.findByMemberIdWithFetch(memberId)
        }
        
        // 기간 필터링 적용
        val filteredTransactions = applyPeriodFilter(transactions, request.period)
        
        // RevenueItemResponse로 변환 (이미 fetch join으로 로딩됨)
        val revenueItems = filteredTransactions.map { transaction ->
            mapToRevenueItemResponse(transaction)
        }
        
        // 이미 DB에서 정렬되어 오므로 추가 정렬 불필요
        
        val summary = calculateRevenueSummaryOptimized(memberId, request.period)
        
        return RevenueListResponse(
            items = revenueItems,
            totalCount = revenueItems.size,
            summary = summary
        )
    }
    
    fun getSettlementInfo(memberId: Long): SettlementInfoResponse {
        // 임시 데이터 - 실제로는 정산 테이블에서 조회
        return SettlementInfoResponse(
            nextSettlementDate = LocalDateTime.of(2024, 9, 1, 0, 0),
            expectedAmount = BigDecimal("3120100"),
            totalSettled = BigDecimal("24567800"),
            accountInfo = AccountInfo(
                bankName = "신한은행",
                accountNumber = "110-123-****47",
                accountHolder = "김도현"
            )
        )
    }
    
    fun getSettlementHistory(memberId: Long): SettlementHistoryResponse {
        // 임시 데이터 - 실제로는 정산 테이블에서 조회
        val settlements = listOf(
            SettlementItem(
                id = 1,
                period = "2024년 7월",
                amount = BigDecimal("2847500"),
                settlementDate = LocalDateTime.of(2024, 8, 1, 0, 0),
                status = SettlementStatus.COMPLETED
            ),
            SettlementItem(
                id = 2,
                period = "2024년 6월",
                amount = BigDecimal("1956300"),
                settlementDate = LocalDateTime.of(2024, 7, 1, 0, 0),
                status = SettlementStatus.COMPLETED
            ),
            SettlementItem(
                id = 3,
                period = "2024년 5월",
                amount = BigDecimal("3245800"),
                settlementDate = LocalDateTime.of(2024, 6, 1, 0, 0),
                status = SettlementStatus.COMPLETED
            )
        )
        
        return SettlementHistoryResponse(
            settlements = settlements,
            totalCount = settlements.size
        )
    }
    
    @Transactional
    fun updateAccountInfo(memberId: Long, accountInfo: AccountInfo): Boolean {
        // 실제로는 회원 계좌 정보 업데이트
        // 1원 인증 프로세스 시작
        return true
    }
    
    @Transactional
    fun createIncomeTransaction(post: AdvertisementPost): RevenueTransaction? {
        try {
            if (post.finalRevenue == null || post.finalRevenue!! <= BigDecimal.ZERO) {
                return null
            }
            
            val member = post.assignment?.publisherMember ?: return null
            
            // assignment를 통해 관련된 order 찾기
            val relatedOrder = post.assignment?.let { assignment ->
                orderRepository.findByAdTaskId(assignment.adTask?.id ?: 0)
            }
            
            val transaction = RevenueTransaction().apply {
                this.member = member
                this.transactionType = TransactionType.INCOME
                this.assignment = post.assignment
                this.order = relatedOrder
                this.orderNumber = relatedOrder?.orderNumber
                this.orderStatus = relatedOrder?.status?.name
                this.roundNumber = post.assignment?.round?.id?.toString()
                this.roundCategory = post.assignment?.round?.title
                this.title = "${post.assignment?.round?.title ?: "광고"} 게시 수익"
                this.amount = post.finalRevenue!!
                this.transactionDate = LocalDate.now()
                this.description = "광고 포스트 게시로 인한 수익 - Round #${post.assignment?.round?.id}"
            }
            
            return revenueTransactionRepository.save(transaction)
        } catch (e: Exception) {
            // 로깅은 하되 예외를 다시 던지지 않음 (포스트 게시 실패 방지)
            println("INCOME 거래 기록 생성 중 오류 발생: ${e.message}")
            return null
        }
    }
    
    @Transactional
    fun createExpenseTransaction(order: Order): RevenueTransaction? {
        try {
            val confirmedPayment = order.payments.firstOrNull { it.paymentStatus.name == "CONFIRMED" }
                ?: return null
                
            val round = order.adTask.round
            val roundId = round.id!!
            val participantsCount = orderRepository.findRoundParticipants(roundId).size
            
            // 입금 확인 시점에는 라운드 전체 비용을 기록 (round.orderAmount)
            val roundOrderAmount = round.orderAmount
            
            val transaction = RevenueTransaction().apply {
                this.member = order.member
                this.transactionType = TransactionType.EXPENSE
                this.order = order
                this.orderNumber = order.orderNumber
                this.orderStatus = order.status.name
                this.roundNumber = roundId.toString()
                this.roundCategory = round.title
                this.title = order.productName
                this.amount = roundOrderAmount
                this.transactionDate = LocalDate.now()
                this.description = "광고 주문 비용 - ${order.orderNumber} (라운드 #${roundId}, ${participantsCount}명 참여)"
            }
            
            return revenueTransactionRepository.save(transaction)
        } catch (e: Exception) {
            // 로깅은 하되 예외를 다시 던지지 않음 (주문 완료 실패 방지)
            println("EXPENSE 거래 기록 생성 중 오류 발생: ${e.message}")
            return null
        }
    }
    
    @Transactional
    fun updateOrderStatusInRevenueTransactions(order: Order): Boolean {
        try {
            // INCOME 거래(매출)만 주문 상태 업데이트
            val incomeTransactions = revenueTransactionRepository.findByOrderIdAndTransactionType(
                order.id!!, 
                TransactionType.INCOME
            )
            
            incomeTransactions.forEach { transaction ->
                transaction.orderStatus = order.status.name
                transaction.updatedAt = LocalDateTime.now()
                revenueTransactionRepository.save(transaction)
            }
            
            println("주문 ${order.orderNumber}의 INCOME revenue_transactions order_status 업데이트 완료 (${incomeTransactions.size}건)")
            return true
        } catch (e: Exception) {
            println("주문 ${order.orderNumber}의 revenue_transactions 업데이트 중 오류 발생: ${e.message}")
            return false
        }
    }
    
    private fun mapToRevenueItemResponse(transaction: RevenueTransaction): RevenueItemResponse {
        return RevenueItemResponse(
            id = transaction.id!!,
            transactionType = transaction.transactionType!!,
            title = transaction.title ?: "",
            description = transaction.description ?: "",
            transactionDate = transaction.transactionDate!!,
            amount = transaction.amount!!,
            memberCompanyName = transaction.member?.companyName ?: "",
            roundNumber = transaction.roundNumber,
            roundCategory = transaction.roundCategory,
            ctrRate = transaction.ctrRate,
            orderNumber = transaction.orderNumber,
            orderStatus = transaction.orderStatus,
            taxInvoiceIssued = transaction.taxInvoiceIssued,
            taxInvoiceNumber = transaction.taxInvoiceNumber,
            createdAt = transaction.createdAt,
            updatedAt = transaction.updatedAt
        )
    }
    
    private fun applyPeriodFilter(
        transactions: List<RevenueTransaction>, 
        period: RevenuePeriod?
    ): List<RevenueTransaction> {
        if (period == null) return transactions
        
        val now = LocalDate.now()
        val startDate = when (period) {
            RevenuePeriod.WEEK -> now.minusWeeks(1)
            RevenuePeriod.MONTH -> now.minusMonths(1)
            RevenuePeriod.QUARTER -> now.minusMonths(3)
            RevenuePeriod.YEAR -> now.minusYears(1)
        }
        
        return transactions.filter { it.transactionDate != null && it.transactionDate!! >= startDate }
    }
    
    private fun calculateRevenueSummary(
        transactions: List<RevenueTransaction>, 
        period: RevenuePeriod?
    ): RevenueSummaryResponse {
        val incomeTransactions = transactions.filter { it.transactionType == TransactionType.INCOME }
        val expenseTransactions = transactions.filter { it.transactionType == TransactionType.EXPENSE }
        
        val totalIncome = incomeTransactions.sumOf { it.amount ?: BigDecimal.ZERO }
        val totalExpense = expenseTransactions.sumOf { it.amount ?: BigDecimal.ZERO }
        val netRevenue = totalIncome - totalExpense
        
        // 이전 기간 데이터 조회 및 성장률 계산
        val (monthlyGrowth, incomeGrowth, expenseGrowth) = calculateGrowthRates(
            totalIncome, totalExpense, netRevenue, period
        )
        
        return RevenueSummaryResponse(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netRevenue = netRevenue,
            monthlyGrowth = monthlyGrowth,
            incomeGrowth = incomeGrowth,
            expenseGrowth = expenseGrowth,
            period = period?.name ?: "ALL"
        )
    }
    
    private fun calculateGrowthRates(
        currentIncome: BigDecimal,
        currentExpense: BigDecimal,
        currentNetRevenue: BigDecimal,
        period: RevenuePeriod?
    ): Triple<Double, Double, Double> {
        if (period == null) return Triple(0.0, 0.0, 0.0)
        
        val now = LocalDate.now()
        val (currentStart, previousStart, previousEnd) = when (period) {
            RevenuePeriod.WEEK -> Triple(
                now.minusWeeks(1),
                now.minusWeeks(2),
                now.minusWeeks(1).minusDays(1)
            )
            RevenuePeriod.MONTH -> Triple(
                now.minusMonths(1),
                now.minusMonths(2),
                now.minusMonths(1).minusDays(1)
            )
            RevenuePeriod.QUARTER -> Triple(
                now.minusMonths(3),
                now.minusMonths(6),
                now.minusMonths(3).minusDays(1)
            )
            RevenuePeriod.YEAR -> Triple(
                now.minusYears(1),
                now.minusYears(2),
                now.minusYears(1).minusDays(1)
            )
        }
        
        // 이전 기간 데이터 조회
        val allTransactions = revenueTransactionRepository.findAll()
        val previousTransactions = allTransactions.filter { transaction ->
            transaction.transactionDate != null && 
            transaction.transactionDate!! >= previousStart && 
            transaction.transactionDate!! <= previousEnd
        }
        
        val previousIncome = previousTransactions
            .filter { transaction -> transaction.transactionType == TransactionType.INCOME }
            .sumOf { transaction -> transaction.amount ?: BigDecimal.ZERO }
        
        val previousExpense = previousTransactions
            .filter { transaction -> transaction.transactionType == TransactionType.EXPENSE }
            .sumOf { transaction -> transaction.amount ?: BigDecimal.ZERO }
        
        val previousNetRevenue = previousIncome - previousExpense
        
        // 성장률 계산 (백분율)
        val incomeGrowth = calculatePercentageGrowth(previousIncome, currentIncome)
        val expenseGrowth = calculatePercentageGrowth(previousExpense, currentExpense)
        val monthlyGrowth = calculatePercentageGrowth(previousNetRevenue, currentNetRevenue)
        
        return Triple(monthlyGrowth, incomeGrowth, expenseGrowth)
    }
    
    // 최적화된 요약 계산 메서드
    private fun calculateRevenueSummaryOptimized(memberId: Long, period: RevenuePeriod?): RevenueSummaryResponse {
        if (period == null) {
            // 전체 기간: 기존 방식 사용
            val totalIncome = revenueTransactionRepository.sumAmountByMemberIdAndTransactionType(memberId, TransactionType.INCOME) ?: BigDecimal.ZERO
            val totalExpense = revenueTransactionRepository.sumAmountByMemberIdAndTransactionType(memberId, TransactionType.EXPENSE) ?: BigDecimal.ZERO
            val netRevenue = totalIncome - totalExpense
            
            return RevenueSummaryResponse(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                netRevenue = netRevenue,
                monthlyGrowth = 0.0,
                incomeGrowth = 0.0,
                expenseGrowth = 0.0,
                period = "ALL"
            )
        }
        
        val now = LocalDate.now()
        val (currentStart, currentEnd) = when (period) {
            RevenuePeriod.WEEK -> Pair(now.minusWeeks(1), now)
            RevenuePeriod.MONTH -> Pair(now.minusMonths(1), now)
            RevenuePeriod.QUARTER -> Pair(now.minusMonths(3), now)
            RevenuePeriod.YEAR -> Pair(now.minusYears(1), now)
        }
        
        // 현재 기간 집계 쿼리 실행
        val currentStats = revenueTransactionRepository.getRevenueStatsByMemberIdAndPeriod(
            memberId, currentStart, currentEnd
        )
        
        var totalIncome = BigDecimal.ZERO
        var totalExpense = BigDecimal.ZERO
        
        currentStats.forEach { stat ->
            val transactionType = stat[0] as TransactionType
            val amount = stat[1] as BigDecimal
            
            when (transactionType) {
                TransactionType.INCOME -> totalIncome = amount
                TransactionType.EXPENSE -> totalExpense = amount
            }
        }
        
        val netRevenue = totalIncome - totalExpense
        
        // 이전 기간과의 비교를 위한 집계 (성장률 계산)
        val (previousStart, previousEnd) = when (period) {
            RevenuePeriod.WEEK -> Pair(now.minusWeeks(2), now.minusWeeks(1))
            RevenuePeriod.MONTH -> Pair(now.minusMonths(2), now.minusMonths(1))
            RevenuePeriod.QUARTER -> Pair(now.minusMonths(6), now.minusMonths(3))
            RevenuePeriod.YEAR -> Pair(now.minusYears(2), now.minusYears(1))
        }
        
        val previousStats = revenueTransactionRepository.getRevenueStatsByMemberIdAndPeriod(
            memberId, previousStart, previousEnd
        )
        
        var previousIncome = BigDecimal.ZERO
        var previousExpense = BigDecimal.ZERO
        
        previousStats.forEach { stat ->
            val transactionType = stat[0] as TransactionType
            val amount = stat[1] as BigDecimal
            
            when (transactionType) {
                TransactionType.INCOME -> previousIncome = amount
                TransactionType.EXPENSE -> previousExpense = amount
            }
        }
        
        val previousNetRevenue = previousIncome - previousExpense
        
        // 성장률 계산
        val incomeGrowth = calculatePercentageGrowth(previousIncome, totalIncome)
        val expenseGrowth = calculatePercentageGrowth(previousExpense, totalExpense)
        val monthlyGrowth = calculatePercentageGrowth(previousNetRevenue, netRevenue)
        
        return RevenueSummaryResponse(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netRevenue = netRevenue,
            monthlyGrowth = monthlyGrowth,
            incomeGrowth = incomeGrowth,
            expenseGrowth = expenseGrowth,
            period = period.name
        )
    }
    
    private fun calculatePercentageGrowth(previous: BigDecimal, current: BigDecimal): Double {
        return if (previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) > 0) 100.0 else 0.0
        } else {
            val growth = (current - previous).divide(previous, 4, java.math.RoundingMode.HALF_UP)
            growth.multiply(BigDecimal(100)).toDouble()
        }
    }
}