package com.service.frame.member.dto

import com.service.frame.member.entity.RentalStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class CompleteRegistrationResponse(
    val success: Boolean = false,
    val message: String = "",
    
    // 회원 정보
    val memberId: Long = 0,
    val email: String = "",
    val companyName: String = "",
    val businessRegistrationNumber: String = "",
    val contactNumber: String = "",
    val isPremium: Boolean = false,
    val rentalStatus: RentalStatus = RentalStatus.INACTIVE,
    
    // 계약 정보
    val contractId: Long = 0,
    val contractDate: LocalDateTime? = null,
    val contractVersion: String = "",
    
    // 임대권 정보
    val rentalRightsId: Long = 0,
    val purchaseDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
    val rentalAmount: BigDecimal = BigDecimal.ZERO,
    
    // 서비스 사용 가능 여부
    val hasValidContract: Boolean = false,
    val hasValidRentalRights: Boolean = false
)