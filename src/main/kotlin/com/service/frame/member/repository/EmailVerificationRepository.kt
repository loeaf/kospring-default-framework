package com.service.frame.member.repository

import com.service.frame.member.entity.EmailVerification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EmailVerificationRepository : JpaRepository<EmailVerification, Long> {
    
    fun findByEmailAndVerificationTokenAndIsVerified(
        email: String, 
        verificationToken: String, 
        isVerified: Boolean
    ): EmailVerification?
    
    fun findByVerificationTokenAndIsVerified(
        verificationToken: String,
        isVerified: Boolean
    ): EmailVerification?
    
    fun findByEmailAndIsVerified(email: String, isVerified: Boolean): EmailVerification?
    
    @Query("SELECT e FROM EmailVerification e WHERE e.email = :email AND e.isVerified = false AND e.expiresAt > :now")
    fun findActiveVerificationByEmail(email: String, now: LocalDateTime = LocalDateTime.now()): EmailVerification?
    
    fun deleteByEmailAndIsVerified(email: String, isVerified: Boolean)
    
    @Query("DELETE FROM EmailVerification e WHERE e.expiresAt < :now")
    fun deleteExpiredVerifications(now: LocalDateTime = LocalDateTime.now())
}