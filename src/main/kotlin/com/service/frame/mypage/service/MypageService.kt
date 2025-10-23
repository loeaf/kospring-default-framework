package com.service.frame.mypage.service

import com.service.frame.mypage.dto.*
import com.service.frame.member.repository.MemberRepository
import com.service.frame.post.repository.AdvertisementPostRepository
import com.service.frame.order.repository.OrderRepository
import com.service.frame.order.entity.OrderStatus
import com.service.frame.ads.service.AdsService
import com.service.frame.revenue.repository.RevenueTransactionRepository
import com.service.frame.revenue.entity.TransactionType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class MypageService(
    private val memberRepository: MemberRepository,
    private val postRepository: AdvertisementPostRepository,
    private val orderRepository: OrderRepository,
    private val adsService: AdsService,
    private val revenueTransactionRepository: RevenueTransactionRepository
) {
    
    fun getUserProfile(memberId: Long): UserProfileResponse {
        val member = memberRepository.findById(memberId).orElse(null)
            ?: throw IllegalArgumentException("회원을 찾을 수 없습니다: $memberId")
        
        val stats = calculateUserStats(memberId)
        
        return UserProfileResponse(
            id = member.id!!,
            name = member.companyName ?: member.email,
            email = member.email,
            membership = MembershipLevel.PREMIUM,
            joinDate = member.createdAt,
            nextPaymentDate = LocalDateTime.now().plusMonths(1),
            stats = stats
        )
    }
    
    fun getMonthlyStats(memberId: Long): MonthlyStatsResponse {
        val now = LocalDateTime.now()
        val startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
        val startOfWeek = now.minusDays(7)
        
        // 주문 통계
        val totalOrders = getTotalOrderCount(memberId)
        val completedOrders = getCompletedOrderCount(memberId)
        val weeklyOrders = getWeeklyOrderCount(memberId, startOfWeek)
        
        // 광고 통계
        val totalAds = getTotalAdCount(memberId)
        val publishedAds = getPublishedAdCount(memberId)
        val weeklyAds = getWeeklyAdCount(memberId, startOfWeek)
        
        // 수익 통계
        val adRevenue = getMonthlyAdRevenue(memberId, startOfMonth)
        val orderRevenue = getMonthlyOrderRevenue(memberId, startOfMonth)
        
        return MonthlyStatsResponse(
            period = now.format(DateTimeFormatter.ofPattern("yyyy년 MM월")),
            orders = OrderStats(
                total = totalOrders,
                completed = completedOrders,
                weekly = weeklyOrders
            ),
            ads = AdStats(
                total = totalAds,
                published = publishedAds,
                weekly = weeklyAds
            ),
            revenue = RevenueStats(
                adRevenue = adRevenue,
                orderRevenue = orderRevenue
            )
        )
    }
    
    fun getAiSettings(memberId: Long): AiSettingsResponse {
        // 실제로는 AI 설정 테이블에서 조회
        return AiSettingsResponse(
            postWriting = false,
            orderWriting = false,
            updatedAt = LocalDateTime.now()
        )
    }
    
    @Transactional
    fun updateAiSettings(memberId: Long, request: AiSettingsUpdateRequest): AiSettingsResponse {
        // 실제로는 AI 설정 테이블 업데이트
        return AiSettingsResponse(
            postWriting = request.postWriting ?: false,
            orderWriting = request.orderWriting ?: false,
            updatedAt = LocalDateTime.now()
        )
    }
    
    fun getAccountInfo(memberId: Long): AccountInfoResponse {
        val member = memberRepository.findById(memberId).orElse(null)
            ?: throw IllegalArgumentException("회원을 찾을 수 없습니다: $memberId")
        
        return AccountInfoResponse(
            membership = "프리미엄",
            membershipLevel = MembershipLevel.PREMIUM,
            joinDate = member.createdAt,
            email = member.email,
            nextPaymentDate = LocalDateTime.now().plusMonths(1),
            isSubscriptionActive = true,
            subscriptionEndDate = LocalDateTime.now().plusMonths(12)
        )
    }
    
    fun getRecentActivities(memberId: Long, limit: Int = 10): RecentActivityResponse {
        val activities = mutableListOf<ActivityItem>()
        
        // 최근 주문 활동
        val recentOrders = getRecentOrderActivities(memberId, limit / 2)
        activities.addAll(recentOrders)
        
        // 최근 광고 활동
        val recentAds = getRecentAdActivities(memberId, limit / 2)
        activities.addAll(recentAds)
        
        // 시간순 정렬
        activities.sortByDescending { it.timestamp }
        
        return RecentActivityResponse(
            activities = activities.take(limit),
            totalCount = activities.size
        )
    }
    
    fun getDashboardSummary(memberId: Long): DashboardSummaryResponse {
        val userProfile = getUserProfile(memberId)
        val monthlyStats = getMonthlyStats(memberId)
        val aiSettings = getAiSettings(memberId)
        val accountInfo = getAccountInfo(memberId)
        val recentActivities = getRecentActivities(memberId, 5).activities
        
        return DashboardSummaryResponse(
            userProfile = userProfile,
            monthlyStats = monthlyStats,
            aiSettings = aiSettings,
            accountInfo = accountInfo,
            recentActivities = recentActivities
        )
    }
    
    fun getRecentAds(memberId: Long, limit: Int = 5): List<com.service.frame.ads.dto.UserAdItem> {
        return adsService.getRecentUserAds(memberId, limit)
    }
    
    fun getNotificationSettings(memberId: Long): NotificationSettingsResponse {
        // 실제로는 알림 설정 테이블에서 조회
        return NotificationSettingsResponse(
            emailNotifications = true,
            smsNotifications = false,
            pushNotifications = true,
            marketingEmails = false,
            orderUpdates = true,
            adPerformanceAlerts = true
        )
    }
    
    @Transactional
    fun updateNotificationSettings(memberId: Long, request: NotificationSettingsUpdateRequest): NotificationSettingsResponse {
        // 실제로는 알림 설정 테이블 업데이트
        return NotificationSettingsResponse(
            emailNotifications = request.emailNotifications ?: true,
            smsNotifications = request.smsNotifications ?: false,
            pushNotifications = request.pushNotifications ?: true,
            marketingEmails = request.marketingEmails ?: false,
            orderUpdates = request.orderUpdates ?: true,
            adPerformanceAlerts = request.adPerformanceAlerts ?: true
        )
    }
    
    private fun calculateUserStats(memberId: Long): UserStats {
        val totalOrders = orderRepository.countByMemberId(memberId).toInt()
        val totalAds = getTotalAdCount(memberId)
        val rounds = orderRepository.countDistinctRoundsByMemberId(memberId).toInt()
        
        return UserStats(
            rounds = rounds,
            totalOrders = totalOrders,
            totalAds = totalAds,
            impressions = 0,  // deprecated - set to 0
            clicks = 0        // deprecated - set to 0
        )
    }
    
    private fun getTotalOrderCount(memberId: Long): Int {
        return orderRepository.countByMemberId(memberId).toInt()
    }
    
    private fun getCompletedOrderCount(memberId: Long): Int {
        return orderRepository.countByMemberIdAndStatus(memberId, OrderStatus.COMPLETED).toInt()
    }
    
    private fun getWeeklyOrderCount(memberId: Long, startOfWeek: LocalDateTime): Int {
        val endOfWeek = LocalDateTime.now()
        return orderRepository.findByMemberIdAndCreatedAtBetween(memberId, startOfWeek, endOfWeek).size
    }
    
    private fun getTotalAdCount(memberId: Long): Int {
        // 실제로는 광고 테이블에서 조회
        val posts = postRepository.findByPublisherMemberIdOrderBySubmittedAtDesc(memberId)
        return posts.size
    }
    
    private fun getPublishedAdCount(memberId: Long): Int {
        // 실제로는 게시된 광고 수 조회
        val posts = postRepository.findByPublisherMemberIdOrderBySubmittedAtDesc(memberId)
        return posts.count { it.postStatus.name == "PUBLISHED" }
    }
    
    private fun getWeeklyAdCount(memberId: Long, startOfWeek: LocalDateTime): Int {
        val posts = postRepository.findByPublisherMemberIdOrderBySubmittedAtDesc(memberId)
        return posts.count { it.submittedAt.isAfter(startOfWeek) }
    }
    
    private fun getMonthlyAdRevenue(memberId: Long, startOfMonth: LocalDateTime): BigDecimal {
        // revenue_transactions 테이블에서 INCOME 거래의 월간 합계
        val startDate = startOfMonth.toLocalDate()
        val endDate = LocalDate.now()
        
        val incomeTransactions = revenueTransactionRepository.findByMemberIdAndTransactionType(
            memberId, 
            TransactionType.INCOME
        )
        
        return incomeTransactions
            .filter { it.transactionDate != null && 
                     it.transactionDate!! >= startDate && 
                     it.transactionDate!! <= endDate }
            .sumOf { it.amount ?: BigDecimal.ZERO }
    }
    
    private fun getMonthlyOrderRevenue(memberId: Long, startOfMonth: LocalDateTime): BigDecimal {
        // revenue_transactions 테이블에서 EXPENSE 거래의 월간 합계  
        val startDate = startOfMonth.toLocalDate()
        val endDate = LocalDate.now()
        
        val expenseTransactions = revenueTransactionRepository.findByMemberIdAndTransactionType(
            memberId, 
            TransactionType.EXPENSE
        )
        
        return expenseTransactions
            .filter { it.transactionDate != null && 
                     it.transactionDate!! >= startDate && 
                     it.transactionDate!! <= endDate }
            .sumOf { it.amount ?: BigDecimal.ZERO }
    }
    
    private fun getRecentOrderActivities(memberId: Long, limit: Int): List<ActivityItem> {
        val orders = orderRepository.findByMemberIdOrderBySubmittedAtDesc(memberId)
        return orders.take(limit).map { order ->
            ActivityItem(
                id = order.id!!,
                type = when (order.status) {
                    OrderStatus.COMPLETED -> ActivityType.ORDER_COMPLETED
                    else -> ActivityType.ORDER_CREATED
                },
                title = "${order.productName} ${when (order.status) {
                    OrderStatus.COMPLETED -> "완료"
                    OrderStatus.IN_PROGRESS -> "진행중"
                    else -> "주문"
                }}",
                description = "주문번호 #${order.orderNumber}",
                timestamp = order.submittedAt,
                status = when (order.status) {
                    OrderStatus.COMPLETED -> "완료"
                    OrderStatus.IN_PROGRESS -> "진행중"
                    OrderStatus.PAYMENT_CONFIRMED -> "결제완료"
                    else -> order.status.name
                }
            )
        }
    }
    
    private fun getRecentAdActivities(memberId: Long, limit: Int): List<ActivityItem> {
        val posts = postRepository.findByPublisherMemberIdOrderBySubmittedAtDesc(memberId)
        return posts.take(limit).map { post ->
            ActivityItem(
                id = post.id!!,
                type = when (post.postStatus.name) {
                    "PUBLISHED" -> ActivityType.AD_PUBLISHED
                    else -> ActivityType.AD_CREATED
                },
                title = "${post.assignment?.round?.title ?: "광고"} ${when (post.postStatus.name) {
                    "PUBLISHED" -> "게시"
                    "SUBMITTED" -> "제출"
                    else -> "작성"
                }}",
                description = "Round #${post.assignment?.round?.id} · ${post.assignment?.round?.category ?: "광고"}",
                timestamp = post.publishedAt ?: post.submittedAt,
                status = when (post.postStatus.name) {
                    "PUBLISHED" -> "게시됨"
                    "SUBMITTED" -> "제출됨"
                    else -> post.postStatus.name
                }
            )
        }
    }
}