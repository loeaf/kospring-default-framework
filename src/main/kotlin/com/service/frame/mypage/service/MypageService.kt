package com.service.frame.mypage.service

import com.service.frame.mypage.dto.*
import com.service.frame.member.repository.MemberRepository
import com.service.frame.post.repository.AdvertisementPostRepository
import com.service.frame.order.repository.OrderRepository
import com.service.frame.ads.service.AdsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class MypageService(
    private val memberRepository: MemberRepository,
    private val postRepository: AdvertisementPostRepository,
    private val orderRepository: OrderRepository,
    private val adsService: AdsService
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
        // 실제 DB에서 계산하는 로직
        return UserStats(
            rounds = 12,
            totalOrders = 45,
            totalAds = 28,
            impressions = 45000,  // deprecated
            clicks = 28000        // deprecated
        )
    }
    
    private fun getTotalOrderCount(memberId: Long): Int {
        // 실제로는 주문 테이블에서 조회
        return 45
    }
    
    private fun getCompletedOrderCount(memberId: Long): Int {
        // 실제로는 완료된 주문 수 조회
        return 42
    }
    
    private fun getWeeklyOrderCount(memberId: Long, startOfWeek: LocalDateTime): Int {
        // 실제로는 이번 주 신규 주문 수 조회
        return 3
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
        // 실제로는 이번 주 신규 광고 수 조회
        return 2
    }
    
    private fun getMonthlyAdRevenue(memberId: Long, startOfMonth: LocalDateTime): BigDecimal {
        // 실제로는 이번 달 광고 수익 계산
        val posts = postRepository.findByPublisherMemberIdOrderBySubmittedAtDesc(memberId)
        return posts
            .filter { it.postStatus.name == "PUBLISHED" && it.publishedAt?.isAfter(startOfMonth) == true }
            .mapNotNull { it.finalRevenue }
            .fold(BigDecimal.ZERO) { acc, revenue -> acc.add(revenue) }
    }
    
    private fun getMonthlyOrderRevenue(memberId: Long, startOfMonth: LocalDateTime): BigDecimal {
        // 실제로는 이번 달 주문 매입 계산
        return BigDecimal("1200000")
    }
    
    private fun getRecentOrderActivities(memberId: Long, limit: Int): List<ActivityItem> {
        // 실제로는 최근 주문 활동 조회
        return listOf(
            ActivityItem(
                id = 1,
                type = ActivityType.ORDER_COMPLETED,
                title = "뷰티 패키지 디자인 완료",
                description = "구매번호 #ORD-2024-002",
                timestamp = LocalDateTime.now().minusDays(1),
                status = "완료"
            )
        )
    }
    
    private fun getRecentAdActivities(memberId: Long, limit: Int): List<ActivityItem> {
        // 실제로는 최근 광고 활동 조회
        return listOf(
            ActivityItem(
                id = 2,
                type = ActivityType.AD_PUBLISHED,
                title = "헬스케어 광고 게시",
                description = "Round #246 · 헬스케어 분야",
                timestamp = LocalDateTime.now().minusDays(2),
                status = "게시됨"
            )
        )
    }
}