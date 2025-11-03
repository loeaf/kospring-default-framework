package com.service.frame.member.dto

import org.springframework.web.multipart.MultipartFile

data class MemberRegistrationRequest(
    val email: String,
    val password: String,
    val companyName: String,
    val businessRegistrationNumber: String,
    val contactNumber: String,
    val businessField: String,
    val productDescription: String,
    val companyDescription: String,
    val businessRegistrationFile: MultipartFile,
    val telecommunicationSalesFile: MultipartFile,
    val advertisingRegistrationFile: MultipartFile, // 광고업등록증 추가
    val verificationCode: String = "" // 이메일 인증 코드 추가
)

// 이메일 인증 요청
data class EmailVerificationRequest(
    val email: String
)

// 이메일 인증 응답
data class EmailVerificationResponse(
    val success: Boolean,
    val message: String,
    val email: String
)

// 이메일 인증 토큰 확인 요청 (기존 호환성을 위해 유지)
data class EmailVerificationConfirmRequest(
    val email: String,
    val verificationCode: String
)

// 이메일 인증 코드 확인 응답
data class EmailVerificationConfirmResponse(
    val success: Boolean,
    val message: String,
    val email: String,
    val isVerified: Boolean
)