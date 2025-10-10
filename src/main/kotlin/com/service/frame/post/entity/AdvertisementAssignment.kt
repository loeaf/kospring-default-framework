package com.service.frame.post.entity

import com.service.frame.ad.entity.AdTask
import com.service.frame.member.entity.Member
import com.service.frame.round.entity.Round
import javax.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "advertisement_assignments")
data class AdvertisementAssignment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    val round: Round? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertiser_member_id", nullable = false)
    val advertiserMember: Member? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_member_id", nullable = false)
    val publisherMember: Member? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_task_id", nullable = false)
    val adTask: AdTask? = null,

    @Column(name = "revenue_per_post", nullable = false, precision = 12, scale = 2)
    val revenuePerPost: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status", nullable = false, length = 20)
    val assignmentStatus: AssignmentStatus = AssignmentStatus.ASSIGNED,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)