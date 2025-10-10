package com.service.frame.post.entity

import com.service.frame.member.entity.Member
import com.service.frame.round.entity.Round
import com.service.frame.ad.entity.AdTask
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "advertisement_posts")
data class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "assignment_id", nullable = false)
    val assignmentId: Long = 0,

    @Column(columnDefinition = "TEXT")
    val content: String? = null,

    @Column(name = "ctr_rate", precision = 5, scale = 2)
    val ctrRate: java.math.BigDecimal? = null,

    @Column(name = "final_revenue", precision = 12, scale = 2)
    val finalRevenue: java.math.BigDecimal? = null,

    @Column(name = "post_status", nullable = false)
    @Enumerated(EnumType.STRING)
    val status: PostStatus = PostStatus.PENDING,

    @Column(name = "submitted_at")
    val submittedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "approved_at")
    val approvedAt: LocalDateTime? = null,

    @Column(name = "published_at")
    val publishedAt: LocalDateTime? = null,

    @Column(name = "failed_at")
    val failedAt: LocalDateTime? = null,

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    val failureReason: String? = null,

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    val rejectionReason: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    val reviewedBy: Member? = null,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    // 추가 필드들 (기존 Post의 필드들을 transient로 처리하거나 계산된 값으로 제공)
    @Transient
    val title: String = "",

    @Transient
    val author: Member = Member(),

    @Transient
    val round: Round = Round(),

    @Transient
    val targetAdTask: AdTask = AdTask(),

    @Transient
    val assignedCost: java.math.BigDecimal = java.math.BigDecimal.ZERO,

    @Transient
    val postType: PostType = PostType.BLOG_INTRODUCTION,

    @Transient
    val postStartDate: LocalDateTime = LocalDateTime.now(),

    @Transient
    val postEndDate: LocalDateTime = LocalDateTime.now(),

    @Transient
    val viewCount: Long = 0,

    @Transient
    val isFeatured: Boolean = false,

    @Transient
    val featuredUntil: LocalDateTime? = null
) {
    fun publish(): Post {
        return this.copy(
            status = PostStatus.PUBLISHED,
            publishedAt = if (publishedAt == null) LocalDateTime.now() else publishedAt,
            updatedAt = LocalDateTime.now()
        )
    }

    fun approve(): Post {
        return this.copy(
            status = PostStatus.APPROVED,
            approvedAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    fun reject(reason: String): Post {
        return this.copy(
            status = PostStatus.REJECTED,
            rejectionReason = reason,
            updatedAt = LocalDateTime.now()
        )
    }

    fun fail(reason: String): Post {
        return this.copy(
            status = PostStatus.FAILED,
            failedAt = LocalDateTime.now(),
            failureReason = reason,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updatePostSchedule(startDate: LocalDateTime, endDate: LocalDateTime): Post {
        return this.copy(
            postStartDate = startDate,
            postEndDate = endDate,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateContent(title: String, content: String): Post {
        return this.copy(
            title = title,
            content = content,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateAssignedCost(newCost: java.math.BigDecimal): Post {
        return this.copy(
            assignedCost = newCost,
            updatedAt = LocalDateTime.now()
        )
    }

    fun isActive(): Boolean {
        val now = LocalDateTime.now()
        return now.isAfter(postStartDate) && now.isBefore(postEndDate)
    }

    fun canBeEditedBy(member: Member): Boolean {
        // @Transient 필드이므로 실제로는 Assignment 정보를 통해 확인해야 함
        // 서비스 레이어에서 Assignment를 조회해서 권한 확인
        return true // 임시로 true 반환, 실제로는 서비스에서 확인
    }

    fun canBeDeletedBy(member: Member): Boolean {
        // @Transient 필드이므로 실제로는 Assignment 정보를 통해 확인해야 함
        // 서비스 레이어에서 Assignment를 조회해서 권한 확인
        return true // 임시로 true 반환, 실제로는 서비스에서 확인
    }

    fun isPublished(): Boolean {
        return status == PostStatus.PUBLISHED
    }

    fun isVisible(): Boolean {
        return status in listOf(PostStatus.PUBLISHED, PostStatus.APPROVED)
    }

    fun incrementViewCount(): Post {
        return this.copy(
            viewCount = viewCount + 1,
            updatedAt = LocalDateTime.now()
        )
    }

    fun setFeatured(featured: Boolean, featuredUntil: LocalDateTime? = null): Post {
        return this.copy(
            isFeatured = featured,
            featuredUntil = if (featured) featuredUntil else null,
            updatedAt = LocalDateTime.now()
        )
    }
}

enum class PostStatus {
    PENDING,    // 대기
    APPROVED,   // 승인
    REJECTED,   // 거절
    PUBLISHED,  // 공개
    FAILED      // 실패
}

enum class PostType {
    BLOG_INTRODUCTION,  // 블로그 소개글
    REVIEW,            // 후기
    TUTORIAL,          // 튜토리얼
    SHOWCASE           // 쇼케이스
}