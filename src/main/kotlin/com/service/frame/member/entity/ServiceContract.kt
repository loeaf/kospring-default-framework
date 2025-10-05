package com.service.frame.member.entity

import javax.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "service_contracts")
@EntityListeners(AuditingEntityListener::class)
data class ServiceContract(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "rental_contract_agreed", nullable = false)
    val rentalContractAgreed: Boolean = false,

    @Column(name = "service_contract_agreed", nullable = false)
    val serviceContractAgreed: Boolean = false,

    @Column(name = "marketing_agreed", nullable = false)
    val marketingAgreed: Boolean = false,

    @Column(name = "contract_date", nullable = false)
    val contractDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "contract_version", nullable = false, length = 10)
    val contractVersion: String = "1.0",

    @Column(name = "ip_address", length = 45)
    val ipAddress: String? = null,

    @Column(name = "user_agent", columnDefinition = "TEXT")
    val userAgent: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    constructor() : this(
        null, Member(), false, false, false, LocalDateTime.now(), 
        "1.0", null, null, true, null, null
    )
}