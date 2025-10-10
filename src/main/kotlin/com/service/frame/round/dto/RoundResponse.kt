package com.service.frame.round.dto

import com.service.frame.round.entity.Round
import com.service.frame.round.entity.RoundStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class RoundResponse(
    val id: Long,
    val roundNumber: String?,
    val title: String,
    val description: String?,
    val category: String?,
    val orderAmount: BigDecimal,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val postStartDate: LocalDateTime?,
    val postEndDate: LocalDateTime?,
    val postDurationDays: Int?,
    val calculatedPostStartDate: LocalDateTime,
    val calculatedPostEndDate: LocalDateTime,
    val status: RoundStatus,
    val maxParticipants: Int?,
    val createdById: Long,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(round: Round): RoundResponse {
            return RoundResponse(
                id = round.id!!,
                roundNumber = round.roundNumber,
                title = round.title,
                description = round.description,
                category = round.category,
                orderAmount = round.orderAmount,
                startDate = round.startDate,
                endDate = round.endDate,
                postStartDate = round.postStartDate,
                postEndDate = round.postEndDate,
                postDurationDays = round.postDurationDays,
                calculatedPostStartDate = round.getCalculatedPostStartDate(),
                calculatedPostEndDate = round.getCalculatedPostEndDate(),
                status = round.status,
                maxParticipants = round.maxParticipants,
                createdById = round.createdBy?.id!!,
                createdAt = round.createdAt!!
            )
        }
    }
}