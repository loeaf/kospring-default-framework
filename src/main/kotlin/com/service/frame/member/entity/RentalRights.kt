package com.service.frame.member.entity

import javax.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalDate

@Entity
@Table(name = "rental_rights")
@EntityListeners(AuditingEntityListener::class)
data class RentalRights(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "purchase_date", nullable = false)
    val purchaseDate: LocalDate,

    @Column(name = "expiry_date", nullable = false)
    val expiryDate: LocalDate,

    @Column(name = "rental_amount", nullable = false, precision = 12, scale = 2)
    val rentalAmount: BigDecimal = BigDecimal("100000000"), // 1억원 고정

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    val status: RentalRightsStatus = RentalRightsStatus.ACTIVE,

    @Column(name = "auto_renewal", nullable = false)
    val autoRenewal: Boolean = false,

    @Column(name = "renewal_notice_sent", nullable = false)
    val renewalNoticeSent: Boolean = false,

    @Column(name = "pricing_preference", nullable = true, length = 20)
    val pricingPreference: String? = null, // highest, lowest, undecided

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    constructor() : this(
        null, Member(), LocalDate.now(), LocalDate.now().plusYears(1), 
        BigDecimal("100000000"), RentalRightsStatus.ACTIVE, false, false, null, null, null
    )
}

enum class RentalRightsStatus {
    ACTIVE,
    EXPIRED,
    CANCELLED
}