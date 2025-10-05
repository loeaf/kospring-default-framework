package com.service.frame.member.dto

data class LoginRequest(
    val email: String = "",
    val password: String = ""
)