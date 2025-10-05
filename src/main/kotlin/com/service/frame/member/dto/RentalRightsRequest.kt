package com.service.frame.member.dto

import java.time.LocalDate

data class RentalRightsRequest(
    val memberId: Long = 0,
    val purchaseDate: LocalDate = LocalDate.now(),
    val durationYears: Int = 1 // 기본 1년
)