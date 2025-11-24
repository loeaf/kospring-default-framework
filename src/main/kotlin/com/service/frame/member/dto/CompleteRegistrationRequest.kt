package com.service.frame.member.dto

import org.springframework.web.multipart.MultipartFile

data class CompleteRegistrationRequest(
    // 회원가입 정보
    val email: String = "",
    val password: String = "",
    val companyName: String = "",
    val businessRegistrationNumber: String = "",
    val contactNumber: String = "",
    val businessField: String = "",
    val productDescription: String = "",
    val companyDescription: String = "",
    val businessRegistrationFile: MultipartFile? = null,
    val telecommunicationSalesFile: MultipartFile? = null,
    val advertisingRegistrationFile: MultipartFile? = null, // 광고업등록증 추가
    
    // 계약 동의 정보
    val rentalContractAgreed: Boolean = false,
    val serviceContractAgreed: Boolean = false,
    val marketingAgreed: Boolean = false,
    
    // 게시비 단가 선택 정보
    val pricingPreference: String = "", // highest, lowest, undecided
    
    // 임대권 정보
    val durationYears: Int = 1,
    
    // 정산 계좌 정보
    val bankCode: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val accountHolder: String = "",
    
    // 기타
    val ipAddress: String? = null,
    val userAgent: String? = null
)