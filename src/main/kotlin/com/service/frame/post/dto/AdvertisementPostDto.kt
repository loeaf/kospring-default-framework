package com.service.frame.post.dto

import java.math.BigDecimal
import java.time.LocalDateTime

// 포스트 내용 작성/수정 요청
data class PostContentUpdateRequest(
    val content: String,
    val notes: String? = null
)

// 포스트 상태 변경 요청
data class PostStatusUpdateRequest(
    val postStatus: String, // PENDING, APPROVED, REJECTED, PUBLISHED, FAILED
    val notes: String? = null,
    val rejectionReason: String? = null,
    val failureReason: String? = null,
    val ctrRate: BigDecimal? = null,
    val finalRevenue: BigDecimal? = null
)

// 내 할당된 포스트 응답
data class MyAssignedPostResponse(
    val assignmentId: Long,
    val postId: Long?,
    val roundId: Long,
    val roundTitle: String,
    val roundStartDate: LocalDateTime?,
    val roundEndDate: LocalDateTime?,
    val advertiserMemberId: Long,
    val advertiserEmail: String,
    val advertiserCompanyName: String?,
    val adTaskId: Long,
    val revenuePerPost: BigDecimal,
    val assignmentStatus: String,
    val postContent: String?,
    val postStatus: String?,
    val ctrRate: BigDecimal?,
    val finalRevenue: BigDecimal?,
    val submittedAt: LocalDateTime?,
    val approvedAt: LocalDateTime?,
    val publishedAt: LocalDateTime?,
    val notes: String?,
    val rejectionReason: String?,
    val createdAt: LocalDateTime
)

// 포스트 상세 응답
data class PostDetailResponse(
    val id: Long,
    val assignmentId: Long,
    val content: String?,
    val ctrRate: BigDecimal?,
    val finalRevenue: BigDecimal?,
    val postStatus: String,
    val submittedAt: LocalDateTime,
    val approvedAt: LocalDateTime?,
    val publishedAt: LocalDateTime?,
    val failedAt: LocalDateTime?,
    val failureReason: String?,
    val rejectionReason: String?,
    val reviewedBy: Long?,
    val reviewedByName: String?,
    val notes: String?,
    val assignment: AssignmentInfo,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AssignmentInfo(
    val id: Long,
    val roundId: Long,
    val roundTitle: String,
    val roundStartDate: LocalDateTime?,
    val roundEndDate: LocalDateTime?,
    val advertiserMemberId: Long,
    val advertiserEmail: String,
    val advertiserCompanyName: String?,
    val publisherMemberId: Long,
    val publisherEmail: String,
    val publisherCompanyName: String?,
    val adTaskId: Long,
    val revenuePerPost: BigDecimal,
    val assignmentStatus: String
)

// 포스트 목록 조회 응답
data class AdvertisementPostListResponse(
    val posts: List<MyAssignedPostResponse>,
    val totalCount: Int,
    val pendingCount: Int,
    val approvedCount: Int,
    val publishedCount: Int,
    val rejectedCount: Int
)