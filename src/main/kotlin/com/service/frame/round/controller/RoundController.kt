package com.service.frame.round.controller

import com.service.frame.round.dto.RoundCreateRequest
import com.service.frame.round.dto.RoundResponse
import com.service.frame.round.entity.RoundStatus
import com.service.frame.round.service.RoundService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/rounds")
class RoundController(
    private val roundService: RoundService
) {

    @PostMapping
    fun createRound(
        @Valid @RequestBody request: RoundCreateRequest,
        @RequestParam createdById: Long
    ): ResponseEntity<RoundResponse> {
        val response = roundService.createRound(request, createdById)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getRounds(
        @RequestParam(required = false) status: RoundStatus?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<RoundResponse>> {
        val response = roundService.getRounds(status, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getRoundById(@PathVariable id: Long): ResponseEntity<RoundResponse> {
        val response = roundService.getRoundById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}/status")
    fun getRoundStatus(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        val round = roundService.getRoundById(id)
        val status = mapOf(
            "roundId" to id,
            "status" to round.status.name,
            "isReady" to (round.status.name == "ACTIVE"),
            "message" to when (round.status.name) {
                "PREPARING" -> "라운드가 준비 중입니다. 잠시 후 활성화됩니다."
                "ACTIVE" -> "라운드가 활성화되었습니다."
                "CLOSED" -> "라운드가 종료되었습니다."
                else -> "알 수 없는 상태입니다."
            }
        )
        return ResponseEntity.ok(status)
    }

    @GetMapping("/member/{memberId}")
    fun getRoundsByCreatedBy(
        @PathVariable memberId: Long,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<RoundResponse>> {
        val response = roundService.getRoundsByCreatedBy(memberId, pageable)
        return ResponseEntity.ok(response)
    }
}