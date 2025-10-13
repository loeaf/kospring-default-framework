package com.service.frame.ads.dto

import java.time.LocalDateTime

data class UserAdsResponse(
    val ads: List<UserAdItem>,
    val totalCount: Int,
    val statusCounts: AdStatusCounts
)

data class UserAdItem(
    val id: Long,
    val roundId: Long,
    val roundTitle: String,
    val status: AdStatus,
    val adContent: String?,
    val htmlFilePath: String?,
    val previewUrl: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val errorMessage: String?,
    val retryCount: Int,
    val metadata: AdMetadata?
)

data class AdMetadata(
    val tags: List<String>?,
    val category: String?,
    val title: String?,
    val description: String?,
    val client: String?,
    val previewHeight: String?
)

enum class AdStatus {
    PENDING,        // 생성 대기
    IN_PROGRESS,    // 생성 중
    COMPLETED,      // 완료
    FAILED,         // 실패
    PUBLISHED       // 게시됨
}

data class AdStatusCounts(
    val pending: Int,
    val inProgress: Int,
    val completed: Int,
    val failed: Int,
    val published: Int
)

data class AdDetailResponse(
    val id: Long,
    val roundId: Long,
    val roundTitle: String,
    val memberId: Long,
    val memberCompanyName: String,
    val memberEmail: String,
    val status: AdStatus,
    val adContent: String?,
    val htmlFilePath: String?,
    val previewUrl: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val errorMessage: String?,
    val retryCount: Int,
    val metadata: AdMetadata?,
    val performance: AdPerformance?
)

data class AdPerformance(
    val views: Long,
    val clicks: Long,
    val ctr: Double,
    val revenue: Double
)

data class AdGalleryResponse(
    val ads: List<AdGalleryItem>,
    val totalCount: Int,
    val categories: List<String>
)

data class AdGalleryItem(
    val id: Long,
    val title: String,
    val description: String,
    val htmlFilePath: String,
    val previewUrl: String,
    val tags: List<String>,
    val category: String,
    val client: String,
    val createdAt: LocalDateTime,
    val previewHeight: String?,
    val isPublic: Boolean
)

data class AdCreateRequest(
    val roundId: Long,
    val title: String?,
    val description: String?,
    val category: String?,
    val tags: List<String>?
)

data class AdUpdateRequest(
    val title: String?,
    val description: String?,
    val category: String?,
    val tags: List<String>?,
    val isPublic: Boolean?
)