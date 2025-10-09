package com.service.frame.ad.dto

import com.service.frame.ad.entity.AdTaskStatus
import java.time.LocalDateTime

data class AdTaskResponse(
    val id: Long,
    val roundId: Long,
    val roundTitle: String,
    val memberId: Long,
    val memberCompanyName: String,
    val memberEmail: String,
    val status: AdTaskStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val errorMessage: String?,
    val retryCount: Int,
    val webUrl: String?
)

data class RoundAdsResponse(
    val roundId: Long,
    val roundTitle: String,
    val totalAds: Int,
    val completedAds: Int,
    val pendingAds: Int,
    val failedAds: Int,
    val ads: List<AdTaskResponse>
)

data class RoundWithAdsResponse(
    val roundId: Long,
    val roundTitle: String,
    val roundDescription: String?,
    val category: String?,
    val orderAmount: java.math.BigDecimal,
    val templateCost: java.math.BigDecimal?,
    val aiGenerationCost: java.math.BigDecimal?,
    val targetingPostingCost: java.math.BigDecimal?,
    val serverRentalCost: java.math.BigDecimal?,
    val otherCosts: java.math.BigDecimal?,
    val startDate: java.time.LocalDateTime,
    val endDate: java.time.LocalDateTime,
    val status: String,
    val maxParticipants: Int?,
    val currentParticipants: Int,
    val remainingTimeMs: Long,
    val ads: List<AdTaskResponse>
)