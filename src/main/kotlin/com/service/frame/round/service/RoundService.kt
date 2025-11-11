package com.service.frame.round.service

import com.service.frame.round.dto.RoundCreateRequest
import com.service.frame.round.dto.RoundResponse
import com.service.frame.round.entity.RoundStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity

interface RoundService {
    fun createRound(request: RoundCreateRequest, createdById: Long): RoundResponse
    fun getRounds(status: RoundStatus?, pageable: Pageable): Page<RoundResponse>
    fun getRoundById(id: Long): RoundResponse
    fun getRoundsByCreatedBy(memberId: Long, pageable: Pageable): Page<RoundResponse>
    fun getPostedAdsForMemberInRound(roundId: Long, memberId: Long): List<Map<String, Any>>
    fun downloadAdFile(filePath: String): ResponseEntity<Any>
}