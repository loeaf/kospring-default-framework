package com.service.frame.member.entity

import javax.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "email_verifications")
data class EmailVerification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "email", nullable = false, length = 255)
    val email: String,

    @Column(name = "verification_token", nullable = false, length = 255)
    val verificationToken: String,

    @Column(name = "is_verified", nullable = false)
    val isVerified: Boolean = false,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "verified_at")
    val verifiedAt: LocalDateTime? = null
) {
    constructor() : this(
        null, "", "", false, LocalDateTime.now().plusMinutes(10), LocalDateTime.now(), null
    )
}