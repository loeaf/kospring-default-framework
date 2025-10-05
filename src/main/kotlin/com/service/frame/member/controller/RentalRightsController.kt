package com.service.frame.member.controller

import com.service.frame.member.dto.RentalRightsRequest
import com.service.frame.member.dto.RentalRightsResponse
import com.service.frame.member.service.RentalRightsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rental-rights")
class RentalRightsController(
    private val rentalRightsService: RentalRightsService
) {

    @PostMapping("/purchase")
    fun purchaseRentalRights(@RequestBody request: RentalRightsRequest): ResponseEntity<RentalRightsResponse> {
        return try {
            val response = rentalRightsService.purchaseRentalRights(request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                RentalRightsResponse(
                    id = 0,
                    memberId = request.memberId,
                    purchaseDate = request.purchaseDate,
                    expiryDate = request.purchaseDate.plusYears(request.durationYears.toLong()),
                    rentalAmount = java.math.BigDecimal("100000000"),
                    status = com.service.frame.member.entity.RentalRightsStatus.CANCELLED,
                    autoRenewal = false,
                    renewalNoticeSent = false,
                    message = e.message ?: "임대권 구매에 실패했습니다.",
                    success = false
                )
            )
        }
    }

    @GetMapping("/member/{memberId}/active")
    fun getActiveRentalRights(@PathVariable memberId: Long): ResponseEntity<List<RentalRightsResponse>> {
        return try {
            val responses = rentalRightsService.getActiveRentalRights(memberId)
            ResponseEntity.ok(responses)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/member/{memberId}/check")
    fun checkValidRentalRights(@PathVariable memberId: Long): ResponseEntity<Map<String, Boolean>> {
        val hasValidRentalRights = rentalRightsService.hasValidRentalRights(memberId)
        return ResponseEntity.ok(mapOf("hasValidRentalRights" to hasValidRentalRights))
    }

    @PostMapping("/{rentalRightsId}/renew")
    fun renewRentalRights(
        @PathVariable rentalRightsId: Long,
        @RequestParam(defaultValue = "1") durationYears: Int
    ): ResponseEntity<RentalRightsResponse> {
        return try {
            val response = rentalRightsService.renewRentalRights(rentalRightsId, durationYears)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                RentalRightsResponse(
                    id = 0,
                    memberId = 0,
                    purchaseDate = java.time.LocalDate.now(),
                    expiryDate = java.time.LocalDate.now(),
                    rentalAmount = java.math.BigDecimal("100000000"),
                    status = com.service.frame.member.entity.RentalRightsStatus.CANCELLED,
                    autoRenewal = false,
                    renewalNoticeSent = false,
                    message = e.message ?: "임대권 갱신에 실패했습니다.",
                    success = false
                )
            )
        }
    }

    @GetMapping("/expiring")
    fun getExpiringRentalRights(
        @RequestParam(defaultValue = "30") days: Int
    ): ResponseEntity<List<RentalRightsResponse>> {
        val responses = rentalRightsService.getExpiringRentalRights(days)
        return ResponseEntity.ok(responses)
    }
}