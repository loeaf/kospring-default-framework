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

    @Column(name = "start_date", nullable = false)
    val startDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "end_date", nullable = false)
    val endDate: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    val status: RoundStatus = RoundStatus.ACTIVE,

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
        BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now(),
        RoundStatus.ACTIVE, null, null, null, null
    )
}


enum class RoundStatus {
    ACTIVE, CLOSED, PENDING
}