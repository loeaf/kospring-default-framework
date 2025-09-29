package com.service.frame.member.entity

import javax.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "members")
@EntityListeners(AuditingEntityListener::class)
data class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 255)
    val email: String,

    @Column(nullable = false, length = 255)
    val password: String,

    @Column(name = "company_name", nullable = false, length = 255)
    val companyName: String,

    @Column(name = "business_registration_number", nullable = false, unique = true, length = 20)
    val businessRegistrationNumber: String,

    @Column(name = "contact_number", nullable = false, length = 20)
    val contactNumber: String,

    @Column(name = "business_registration_file", nullable = false, length = 500)
    val businessRegistrationFile: String,

    @Column(name = "telecommunication_sales_file", nullable = false, length = 500)
    val telecommunicationSalesFile: String,

    @Column(name = "is_premium", nullable = false)
    val isPremium: Boolean = false,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)