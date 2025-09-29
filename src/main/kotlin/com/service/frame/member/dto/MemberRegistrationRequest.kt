package com.service.frame.member.dto

import org.springframework.web.multipart.MultipartFile

data class MemberRegistrationRequest(
    val email: String,
    val password: String,
    val companyName: String,
    val businessRegistrationNumber: String,
    val contactNumber: String,
    val businessRegistrationFile: MultipartFile,
    val telecommunicationSalesFile: MultipartFile
)