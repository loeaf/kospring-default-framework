package com.service.frame.revenue.service

import com.service.frame.revenue.dto.*
import com.service.frame.post.repository.AdvertisementPostRepository
import com.service.frame.order.repository.OrderRepository
import com.service.frame.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class RevenueService(
    private val postRepository: AdvertisementPostRepository,
    private val orderRepository: OrderRepository,
    private val memberRepository: MemberRepository
) {
    
    fun getRevenueList(request: RevenueFilterRequest, memberId: Long): RevenueListResponse {
        val revenueItems = mutableListOf<RevenueItemResponse>()
        
        // 광고 수익 조회 (매출)
        if (request.type == null || request.type == RevenueType.SALES) {
            val adRevenues = getAdvertisementRevenues(memberId, request)
            revenueItems.addAll(adRevenues)
        }
        
        // 주문 수익 조회 (매입)
        if (request.type == null || request.type == RevenueType.PURCHASES) {
            val orderRevenues = getOrderRevenues(memberId, request)
            revenueItems.addAll(orderRevenues)
        }
        
        // 날짜 순으로 정렬
        revenueItems.sortByDescending { it.date }
        
        val summary = calculateRevenueSummary(revenueItems, request.period)
        
        return RevenueListResponse(
            items = revenueItems,
            totalCount = revenueItems.size,
            summary = summary
        )
    }
    
    private fun getAdvertisementRevenues(memberId: Long, request: RevenueFilterRequest): List<RevenueItemResponse> {
        val posts = postRepository.findByPublisherMemberIdOrderBySubmittedAtDesc(memberId)
        
        return posts.filter { post ->
            post.postStatus.name == "PUBLISHED" && 
            post.finalRevenue != null && 
            post.finalRevenue!! > BigDecimal.ZERO
        }.map { post ->
            val revenue = post.finalRevenue!!
            val fee = revenue.multiply(BigDecimal("0.10")) // 10% 수수료
            val netRevenue = revenue.subtract(fee)
            
            RevenueItemResponse(
                id = "ad_${post.id}",
                type = RevenueType.SALES,
                title = "${post.assignment?.round?.title ?: "광고"} 수익",
                description = "Round #${post.assignment?.round?.id} · ${post.assignment?.advertiserMember?.companyName ?: "광고주"}",
                date = post.publishedAt ?: post.submittedAt,
                amount = netRevenue,
                status = "완료",
                fee = fee,
                metrics = listOf(
                    RevenueMetric("예상 수익", "₩${netRevenue.toInt().toString().reversed().chunked(3).joinToString(",").reversed()}"),
                    RevenueMetric("수익률", "10.0%")
                ),
                category = "광고"
            )
        }
    }
    
    private fun getOrderRevenues(memberId: Long, request: RevenueFilterRequest): List<RevenueItemResponse> {
        // 임시 데이터 - 실제로는 주문 테이블에서 조회
        return listOf(
            RevenueItemResponse(
                id = "order_1",
                type = RevenueType.PURCHASES,
                title = "뷰티 패키지 디자인",
                description = "구매번호 #ORD-2024-002",
                date = LocalDateTime.now().minusDays(1),
                amount = BigDecimal("1200000"),
                status = "완료",
                fee = BigDecimal("20000"),
                metrics = listOf(
                    RevenueMetric("예상 수익", "₩200,000"),
                    RevenueMetric("수익률", "16.7%")
                ),
                category = "디자인"
            ),
            RevenueItemResponse(
                id = "order_2",
                type = RevenueType.PURCHASES,
                title = "교육 플랫폼 UI 제작",
                description = "구매번호 #ORD-2024-004",
                date = LocalDateTime.now().minusDays(3),
                amount = BigDecimal("3200000"),
                status = "완료",
                fee = BigDecimal("15000"),
                metrics = listOf(
                    RevenueMetric("예상 수익", "₩150,000"),
                    RevenueMetric("수익률", "4.7%")
                ),
                category = "개발"
            )
        )
    }
    
    private fun calculateRevenueSummary(items: List<RevenueItemResponse>, period: RevenuePeriod): RevenueSummaryResponse {
        val totalRevenue = items.sumOf { it.amount }
        val adRevenue = items.filter { it.type == RevenueType.SALES }.sumOf { it.amount }
        val orderRevenue = items.filter { it.type == RevenueType.PURCHASES }.sumOf { it.amount }
        
        return RevenueSummaryResponse(
            totalRevenue = totalRevenue,
            monthlyGrowth = BigDecimal("12.5"),
            adRevenue = adRevenue,
            adRevenueGrowth = BigDecimal("8.3"),
            orderRevenue = orderRevenue,
            orderRevenueGrowth = BigDecimal("25.0"),
            period = period.name
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
}