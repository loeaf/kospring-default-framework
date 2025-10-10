package com.service.frame.post.entity

import com.service.frame.member.entity.Member
import javax.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "advertisement_posts")
data class AdvertisementPost(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    val assignment: AdvertisementAssignment? = null,

    @Column(name = "content", columnDefinition = "TEXT")
    val content: String? = null,

    @Column(name = "ctr_rate", precision = 5, scale = 2)
    val ctrRate: BigDecimal? = null,

    @Column(name = "final_revenue", precision = 12, scale = 2)
    val finalRevenue: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", nullable = false, length = 20)
    val postStatus: AdvertisementPostStatus = AdvertisementPostStatus.PENDING,

    @Column(name = "submitted_at", nullable = false)
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

    @Column(name = "notes", columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class AdvertisementPostStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PUBLISHED,
    FAILED
}