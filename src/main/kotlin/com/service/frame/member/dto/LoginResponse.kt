package com.service.frame.member.dto

import com.service.frame.member.entity.RentalStatus
import java.time.LocalDate

data class LoginResponse(
    val id: Long,
    val email: String,
    val companyName: String,
    val businessRegistrationNumber: String,
    val contactNumber: String,
    val isPremium: Boolean,
    val rentalStatus: RentalStatus,
    val currentRentalExpiry: LocalDate?,
    val hasValidContract: Boolean,
    val message: String,
    val success: Boolean
)