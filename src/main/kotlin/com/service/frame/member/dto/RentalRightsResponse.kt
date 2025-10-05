package com.service.frame.member.dto

import com.service.frame.member.entity.RentalRightsStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class RentalRightsResponse(
    val id: Long,
    val memberId: Long,
    val purchaseDate: LocalDate,
    val expiryDate: LocalDate,
    val rentalAmount: BigDecimal,
    val status: RentalRightsStatus,
    val autoRenewal: Boolean,
    val renewalNoticeSent: Boolean,
    val message: String,
    val success: Boolean
)