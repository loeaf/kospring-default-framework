package com.service.frame.member.dto

data class MemberRegistrationResponse(
    val id: Long,
    val email: String,
    val companyName: String,
    val businessRegistrationNumber: String,
    val contactNumber: String,
    val isPremium: Boolean,
    val message: String
)