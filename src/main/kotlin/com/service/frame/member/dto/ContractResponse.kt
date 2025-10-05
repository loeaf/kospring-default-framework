package com.service.frame.member.dto

import java.time.LocalDateTime

data class ContractResponse(
    val id: Long,
    val memberId: Long,
    val rentalContractAgreed: Boolean,
    val serviceContractAgreed: Boolean,
    val marketingAgreed: Boolean,
    val contractDate: LocalDateTime,
    val contractVersion: String,
    val isActive: Boolean,
    val message: String,
    val success: Boolean
)