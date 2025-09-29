package com.service.frame.member.dto

data class ValidationResponse(
    val isAvailable: Boolean,
    val message: String
)