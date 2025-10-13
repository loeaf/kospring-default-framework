package com.service.frame.mypage.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class UserProfileResponse(
    val id: Long,
    val name: String,
    val email: String,
    val membership: MembershipLevel,
    val joinDate: LocalDateTime?,
    val nextPaymentDate: LocalDateTime?,
    val stats: UserStats
)

data class UserStats(
    val rounds: Int,                // 참여한 라운드 수
    val totalOrders: Int,           // 총 주문 수
    val totalAds: Int,              // 총 광고 수
    val impressions: Long,          // 총 노출수 (deprecated, totalOrders로 대체)
    val clicks: Long                // 총 클릭수 (deprecated, totalAds로 대체)
)

enum class MembershipLevel {
    BASIC,
    PREMIUM,
    VIP
}

data class MonthlyStatsResponse(
    val period: String,
    val orders: OrderStats,
    val ads: AdStats,
    val revenue: RevenueStats
)

data class OrderStats(
    val total: Int,         // 총 주문 수
    val completed: Int,     // 완료된 주문 수
    val weekly: Int         // 이번 주 신규 주문 수
)

data class AdStats(
    val total: Int,         // 총 광고 수
    val published: Int,     // 게시된 광고 수
    val weekly: Int         // 이번 주 신규 광고 수
)

data class RevenueStats(
    val adRevenue: BigDecimal,      // 광고 매출
    val orderRevenue: BigDecimal    // 주문 매입
)

data class AiSettingsResponse(
    val postWriting: Boolean,
    val orderWriting: Boolean,
    val updatedAt: LocalDateTime
)

data class AiSettingsUpdateRequest(
    val postWriting: Boolean?,
    val orderWriting: Boolean?
)

data class AccountInfoResponse(
    val membership: String,
    val membershipLevel: MembershipLevel,
    val joinDate: LocalDateTime?,
    val email: String,
    val nextPaymentDate: LocalDateTime?,
    val isSubscriptionActive: Boolean,
    val subscriptionEndDate: LocalDateTime?
)

data class RecentActivityResponse(
    val activities: List<ActivityItem>,
    val totalCount: Int
)

data class ActivityItem(
    val id: Long,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: LocalDateTime,
    val status: String,
    val metadata: Map<String, Any>? = null
)

enum class ActivityType {
    ORDER_CREATED,      // 주문 생성
    ORDER_COMPLETED,    // 주문 완료
    AD_CREATED,         // 광고 생성
    AD_PUBLISHED,       // 광고 게시
    REVENUE_EARNED,     // 수익 발생
    PAYMENT_PROCESSED   // 결제 처리
}

data class DashboardSummaryResponse(
    val userProfile: UserProfileResponse,
    val monthlyStats: MonthlyStatsResponse,
    val aiSettings: AiSettingsResponse,
    val accountInfo: AccountInfoResponse,
    val recentActivities: List<ActivityItem>
)

data class NotificationSettingsResponse(
    val emailNotifications: Boolean,
    val smsNotifications: Boolean,
    val pushNotifications: Boolean,
    val marketingEmails: Boolean,
    val orderUpdates: Boolean,
    val adPerformanceAlerts: Boolean
)

data class NotificationSettingsUpdateRequest(
    val emailNotifications: Boolean?,
    val smsNotifications: Boolean?,
    val pushNotifications: Boolean?,
    val marketingEmails: Boolean?,
    val orderUpdates: Boolean?,
    val adPerformanceAlerts: Boolean?
)