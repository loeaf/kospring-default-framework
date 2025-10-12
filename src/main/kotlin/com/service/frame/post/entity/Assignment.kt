package com.service.frame.post.entity

import com.service.frame.member.entity.Member
import com.service.frame.round.entity.Round
import com.service.frame.ad.entity.AdTask
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "advertisement_assignments")
data class Assignment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    val round: Round = Round(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertiser_member_id", nullable = false)
    val advertiserMember: Member = Member(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_member_id", nullable = false)
    val publisherMember: Member = Member(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_task_id", nullable = false)
    val adTask: AdTask = AdTask(),

    @Column(name = "revenue_per_post", nullable = false, precision = 12, scale = 2)
    val revenuePerPost: BigDecimal = BigDecimal.ZERO,

    @Column(name = "assignment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    val assignmentStatus: AssignmentStatus = AssignmentStatus.ASSIGNED,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class AssignmentStatus {
    ASSIGNED,       // 할당됨
    WRITTEN,    // 진행중
    COMPLETED,      // 완료
    FAILED          // 실패
}