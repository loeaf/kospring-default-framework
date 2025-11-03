package com.service.frame.member.entity

import javax.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.LocalDate

@Entity
@Table(name = "members")
@EntityListeners(AuditingEntityListener::class)
data class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 255)
    val email: String = "",

    @Column(nullable = false, length = 255)
    val password: String = "",

    @Column(name = "company_name", nullable = false, length = 255)
    val companyName: String = "",

    @Column(name = "business_registration_number", nullable = false, unique = true, length = 20)
    val businessRegistrationNumber: String = "",

    @Column(name = "contact_number", nullable = false, length = 20)
    val contactNumber: String = "",

    @Column(name = "business_field", nullable = false, length = 255)
    val businessField: String = "",

    @Column(name = "product_description", nullable = false, length = 1000)
    val productDescription: String = "",

    @Column(name = "company_description", nullable = false, length = 1000)
    val companyDescription: String = "",

    @Column(name = "business_registration_file", nullable = false, length = 500)
    val businessRegistrationFile: String = "",

    @Column(name = "telecommunication_sales_file", nullable = false, length = 500)
    val telecommunicationSalesFile: String = "",

    @Column(name = "advertising_registration_file", nullable = false, length = 500)
    val advertisingRegistrationFile: String = "",

    @Column(name = "is_premium", nullable = false)
    val isPremium: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_status", nullable = false, length = 20)
    val rentalStatus: RentalStatus = RentalStatus.INACTIVE,


    @Column(name = "current_rental_expiry")
    val currentRentalExpiry: LocalDate? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {
    constructor() : this(
        null, "", "", "", "", "", "", "", "", "", "", "", false, RentalStatus.INACTIVE, null, null, null
    )
}

enum class RentalStatus {
    ACTIVE,
    EXPIRED,
    INACTIVE
}