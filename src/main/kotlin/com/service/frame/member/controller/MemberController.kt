package com.service.frame.member.controller

import com.service.frame.member.dto.MemberRegistrationRequest
import com.service.frame.member.dto.MemberRegistrationResponse
import com.service.frame.member.dto.ValidationResponse
import com.service.frame.member.service.MemberService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService
) {

    @PostMapping("/register")
    fun registerMember(
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam companyName: String,
        @RequestParam businessRegistrationNumber: String,
        @RequestParam contactNumber: String,
        @RequestParam businessRegistrationFile: MultipartFile,
        @RequestParam telecommunicationSalesFile: MultipartFile
    ): ResponseEntity<MemberRegistrationResponse> {
        return try {
            val request = MemberRegistrationRequest(
                email = email,
                password = password,
                companyName = companyName,
                businessRegistrationNumber = businessRegistrationNumber,
                contactNumber = contactNumber,
                businessRegistrationFile = businessRegistrationFile,
                telecommunicationSalesFile = telecommunicationSalesFile
            )

            val response = memberService.registerMember(request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                MemberRegistrationResponse(
                    id = 0,
                    email = email,
                    companyName = companyName,
                    businessRegistrationNumber = businessRegistrationNumber,
                    contactNumber = contactNumber,
                    isPremium = false,
                    message = e.message ?: "잘못된 요청입니다."
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                MemberRegistrationResponse(
                    id = 0,
                    email = email,
                    companyName = companyName,
                    businessRegistrationNumber = businessRegistrationNumber,
                    contactNumber = contactNumber,
                    isPremium = false,
                    message = "서버 오류가 발생했습니다."
                )
            )
        }
    }

    @GetMapping("/check-email")
    fun checkEmailAvailability(@RequestParam email: String): ResponseEntity<ValidationResponse> {
        val response = memberService.checkEmailAvailability(email)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/check-business-number")
    fun checkBusinessRegistrationNumberAvailability(
        @RequestParam businessRegistrationNumber: String
    ): ResponseEntity<ValidationResponse> {
        val response = memberService.checkBusinessRegistrationNumberAvailability(businessRegistrationNumber)
        return ResponseEntity.ok(response)
    }
}