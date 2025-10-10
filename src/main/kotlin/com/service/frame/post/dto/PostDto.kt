package com.service.frame.post.dto

import com.service.frame.post.entity.Post
import com.service.frame.post.entity.PostStatus
import com.service.frame.post.entity.PostType
import java.math.BigDecimal
import java.time.LocalDateTime

data class PostCreateRequest(
    val title: String,
    val content: String,
    val roundId: Long,
    val targetAdTaskId: Long,
    val assignedCost: BigDecimal? = null,
    val postType: PostType = PostType.BLOG_INTRODUCTION,
    val status: PostStatus = PostStatus.PENDING
)

data class PostUpdateRequest(
    val title: String?,
    val content: String?,
    val assignedCost: BigDecimal?,
    val postType: PostType?,
    val status: PostStatus?
)

data class PostResponse(
    val id: Long,
    val title: String,
    val content: String?,
    val authorId: Long,
    val authorName: String,
    val roundId: Long,
    val roundTitle: String,
    val targetAdTaskId: Long,
    val targetAdContent: String?,
    val assignedCost: BigDecimal,
    val postType: PostType,
    val status: PostStatus,
    val postStartDate: LocalDateTime,
    val postEndDate: LocalDateTime,
    val publishedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isActive: Boolean
) {
    companion object {
        fun from(post: Post): PostResponse {
            // @Transient 필드들은 기본값으로 설정
            return PostResponse(
                id = post.id!!,
                title = post.title,
                content = post.content ?: "",
                authorId = post.author.id ?: 0L,
                authorName = post.author.companyName ?: "Unknown",
                roundId = post.round.id ?: 0L,
                roundTitle = post.round.title ?: "Unknown Round",
                targetAdTaskId = post.targetAdTask.id ?: 0L,
                targetAdContent = post.targetAdTask.adContent ?: "",
                assignedCost = post.finalRevenue ?: post.assignedCost,
                postType = post.postType,
                status = post.status,
                postStartDate = post.postStartDate,
                postEndDate = post.postEndDate,
                publishedAt = post.publishedAt,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt,
                isActive = post.isActive()
            )
        }
    }
}

data class PostListResponse(
    val id: Long,
    val title: String,
    val authorId: Long,
    val authorName: String,
    val roundId: Long,
    val roundTitle: String,
    val targetAdTaskId: Long,
    val assignedCost: BigDecimal,
    val postType: PostType,
    val status: PostStatus,
    val postStartDate: LocalDateTime,
    val postEndDate: LocalDateTime,
    val publishedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isActive: Boolean
) {
    companion object {
        fun from(post: Post): PostListResponse {
            return PostListResponse(
                id = post.id!!,
                title = post.title,
                authorId = post.author.id ?: 0L,
                authorName = post.author.companyName ?: "Unknown",
                roundId = post.round.id ?: 0L,
                roundTitle = post.round.title ?: "Unknown Round",
                targetAdTaskId = post.targetAdTask.id ?: 0L,
                assignedCost = post.finalRevenue ?: post.assignedCost,
                postType = post.postType,
                status = post.status,
                postStartDate = post.postStartDate,
                postEndDate = post.postEndDate,
                publishedAt = post.publishedAt,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt,
                isActive = post.isActive()
            )
        }
    }
}

data class PostSearchRequest(
    val keyword: String? = null,
    val status: PostStatus? = null,
    val authorId: Long? = null,
    val roundId: Long? = null,
    val targetAdTaskId: Long? = null,
    val postType: PostType? = null,
    val minCost: BigDecimal? = null,
    val maxCost: BigDecimal? = null,
    val page: Int = 0,
    val size: Int = 20
)

data class PostStatsResponse(
    val totalPosts: Long,
    val publishedPosts: Long,
    val draftPosts: Long,
    val hiddenPosts: Long
)

data class AdPostStatsResponse(
    val adTaskId: Long,
    val roundId: Long,
    val postCount: Long,
    val totalAssignedCost: BigDecimal,
    val avgCostPerPost: BigDecimal
)

data class RoundPostStatsResponse(
    val roundId: Long,
    val roundTitle: String,
    val totalPosts: Long,
    val publishedPosts: Long,
    val totalAssignedCost: BigDecimal,
    val uniqueAuthors: Long,
    val coveredAds: Long,
    val postStartDate: LocalDateTime,
    val postEndDate: LocalDateTime
)

data class CircularAssignmentResponse(
    val roundId: Long,
    val assignments: List<AssignmentDetail>,
    val totalCost: BigDecimal,
    val isBalanced: Boolean
)

data class AssignmentDetail(
    val authorId: Long,
    val authorName: String,
    val targetAdTaskId: Long,
    val assignedCost: BigDecimal,
    val postId: Long? = null,
    val isCompleted: Boolean = false
)