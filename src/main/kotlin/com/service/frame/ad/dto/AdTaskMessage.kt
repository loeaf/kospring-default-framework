package com.service.frame.ad.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Redis 큐에서 전송할 광고 생성 작업 메시지
 */
data class AdTaskMessage(
    @JsonProperty("taskId")
    val taskId: Long,
    
    @JsonProperty("roundId")
    val roundId: Long,
    
    @JsonProperty("memberId")
    val memberId: Long,
    
    @JsonProperty("roundInfo")
    val roundInfo: RoundInfo,
    
    @JsonProperty("memberInfo")
    val memberInfo: MemberInfo,
    
    @JsonProperty("createdAt")
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class RoundInfo(
    @JsonProperty("id")
    val id: Long,
    
    @JsonProperty("title")
    val title: String,
    
    @JsonProperty("description")
    val description: String?,
    
    @JsonProperty("category")
    val category: String?,
    
    @JsonProperty("orderAmount")
    val orderAmount: BigDecimal,
    
    @JsonProperty("templateCost")
    val templateCost: BigDecimal?,
    
    @JsonProperty("aiGenerationCost")
    val aiGenerationCost: BigDecimal?,
    
    @JsonProperty("targetingPostingCost")
    val targetingPostingCost: BigDecimal?,
    
    @JsonProperty("serverRentalCost")
    val serverRentalCost: BigDecimal?,
    
    @JsonProperty("otherCosts")
    val otherCosts: BigDecimal?,
    
    @JsonProperty("startDate")
    val startDate: LocalDateTime,
    
    @JsonProperty("endDate")
    val endDate: LocalDateTime,
    
    @JsonProperty("maxParticipants")
    val maxParticipants: Int?
)

data class MemberInfo(
    @JsonProperty("id")
    val id: Long,
    
    @JsonProperty("email")
    val email: String,
    
    @JsonProperty("companyName")
    val companyName: String,
    
    @JsonProperty("businessRegistrationNumber")
    val businessRegistrationNumber: String,
    
    @JsonProperty("contactNumber")
    val contactNumber: String,
    
    @JsonProperty("businessField")
    val businessField: String,
    
    @JsonProperty("companyDescription")
    val companyDescription: String,
    
    @JsonProperty("productDescription")
    val productDescription: String
)