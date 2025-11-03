package com.service.frame.member.service

import com.service.frame.member.dto.*
import com.service.frame.member.entity.EmailVerification
import com.service.frame.member.repository.EmailVerificationRepository
import com.service.frame.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class EmailVerificationService(
    private val emailVerificationRepository: EmailVerificationRepository,
    private val memberRepository: MemberRepository
) {

    /**
     * 이메일 인증 링크 발송
     */
    fun sendVerificationLink(request: EmailVerificationRequest): EmailVerificationResponse {
        val email = request.email.trim().lowercase()
        
        // 이미 가입된 이메일인지 확인
        if (memberRepository.findByEmail(email) != null) {
            return EmailVerificationResponse(
                success = false,
                message = "이미 가입된 이메일입니다.",
                email = email
            )
        }
        
        // 기존 미인증 토큰 삭제
        emailVerificationRepository.deleteByEmailAndIsVerified(email, false)
        
        // 고유한 인증 토큰 생성
        val verificationToken = generateVerificationToken()
        
        // 인증 정보 저장 (30분 유효)
        val emailVerification = EmailVerification(
            email = email,
            verificationToken = verificationToken,
            expiresAt = LocalDateTime.now().plusMinutes(30)
        )
        
        emailVerificationRepository.save(emailVerification)
        
        // 실제로는 이메일 발송 서비스를 호출해야 함
        // 현재는 로그로 대체
        val verificationUrl = "http://localhost:8080/api/members/email/verify?token=$verificationToken"
        println("이메일 인증 링크 발송: $email -> $verificationUrl")
        
        return EmailVerificationResponse(
            success = true,
            message = "인증 링크가 이메일로 발송되었습니다. 30분 내에 클릭해주세요.",
            email = email
        )
    }

    /**
     * 토큰을 통한 이메일 인증
     */
    fun verifyToken(token: String): EmailVerificationConfirmResponse {
        // 유효한 인증 토큰 조회
        val verification = emailVerificationRepository.findByVerificationTokenAndIsVerified(
            token, false
        )
        
        if (verification == null) {
            return EmailVerificationConfirmResponse(
                success = false,
                message = "유효하지 않은 인증 링크입니다.",
                email = "",
                isVerified = false
            )
        }
        
        // 만료 확인
        if (verification.expiresAt.isBefore(LocalDateTime.now())) {
            return EmailVerificationConfirmResponse(
                success = false,
                message = "인증 링크가 만료되었습니다. 새로운 링크를 요청해주세요.",
                email = verification.email,
                isVerified = false
            )
        }
        
        // 인증 완료 처리
        val verifiedVerification = verification.copy(
            isVerified = true,
            verifiedAt = LocalDateTime.now()
        )
        
        emailVerificationRepository.save(verifiedVerification)
        
        return EmailVerificationConfirmResponse(
            success = true,
            message = "이메일 인증이 완료되었습니다.",
            email = verification.email,
            isVerified = true
        )
    }

    /**
     * 이메일 인증 상태 확인
     */
    fun isEmailVerified(email: String): Boolean {
        val verification = emailVerificationRepository.findByEmailAndIsVerified(
            email.trim().lowercase(), true
        )
        return verification != null
    }

    /**
     * 고유한 인증 토큰 생성
     */
    private fun generateVerificationToken(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * 만료된 인증 토큰 정리 (스케줄러에서 호출)
     */
    fun cleanupExpiredVerifications() {
        emailVerificationRepository.deleteExpiredVerifications()
    }
}