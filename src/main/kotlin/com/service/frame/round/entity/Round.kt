package com.service.frame.round.entity

import com.service.frame.member.entity.Member
import javax.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "rounds")
@EntityListeners(AuditingEntityListener::class)
data class Round(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "round_number", unique = true, length = 20)
    var roundNumber: String? = null,

    @Column(nullable = false, length = 255)
    val title: String = "",

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(length = 100)
    val category: String? = null,

    @Column(name = "order_amount", nullable = false, precision = 12, scale = 2)
    val orderAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "template_cost", precision = 12, scale = 2)
    val templateCost: BigDecimal? = null,

    @Column(name = "ai_generation_cost", precision = 12, scale = 2)
    val aiGenerationCost: BigDecimal? = null,

    @Column(name = "targeting_posting_cost", precision = 12, scale = 2)
    val targetingPostingCost: BigDecimal? = null,

    @Column(name = "server_rental_cost", precision = 12, scale = 2)
    val serverRentalCost: BigDecimal? = null,

    @Column(name = "other_costs", precision = 12, scale = 2)
    val otherCosts: BigDecimal? = null,

    @Column(name = "start_date", nullable = false)
    val startDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "end_date", nullable = false)
    val endDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "post_start_date")
    val postStartDate: LocalDateTime? = null,

    @Column(name = "post_end_date")
    val postEndDate: LocalDateTime? = null,

    @Column(name = "post_duration_days")
    val postDurationDays: Int? = 7,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    val status: RoundStatus = RoundStatus.PREPARING,

    @Column(name = "max_participants")
    val maxParticipants: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    val createdBy: Member? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    constructor() : this(
        null, null, "", null, null,
        BigDecimal.ZERO, null, null, null, null, null,
        LocalDateTime.now(), LocalDateTime.now(),
        null, null, 7,
        RoundStatus.PREPARING, null, null, null, null
    )

    // 게시 시작일 계산 (라운드 종료일 = 게시 시작일)
    fun getCalculatedPostStartDate(): LocalDateTime {
        return postStartDate ?: endDate
    }

    // 게시 종료일 계산 (게시 시작일 + duration)
    fun getCalculatedPostEndDate(): LocalDateTime {
        val duration = postDurationDays ?: 7
        return postEndDate ?: getCalculatedPostStartDate().plusDays(duration.toLong())
    }
}


enum class RoundStatus {
    PREPARING,    // 준비중 -> 라운드는 생성되었으나 광고가 제작되지 않은 단계
    ACTIVE,       // 광고 판매중 -> 라운드는 생성되었고 광고역시 제작된 단계
    CLOSED        // 완료
}