package com.service.frame.member.dto

data class ContractRequest(
    val memberId: Long = 0,
    val rentalContractAgreed: Boolean = false,
    val serviceContractAgreed: Boolean = false,
    val marketingAgreed: Boolean = false,
    val ipAddress: String? = null,
    val userAgent: String? = null
)