package com.service.frame.member.controller

import com.service.frame.member.dto.*
import com.service.frame.member.entity.RentalStatus
import com.service.frame.member.service.MemberService
import com.service.frame.member.service.EmailVerificationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
    private val emailVerificationService: EmailVerificationService
) {

    @PostMapping("/register")
    fun registerMember(
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam companyName: String,
        @RequestParam businessRegistrationNumber: String,
        @RequestParam contactNumber: String,
        @RequestParam businessField: String,
        @RequestParam productDescription: String,
        @RequestParam companyDescription: String,
        @RequestParam businessRegistrationFile: MultipartFile,
        @RequestParam telecommunicationSalesFile: MultipartFile,
        @RequestParam advertisingRegistrationFile: MultipartFile,
        @RequestParam(required = false, defaultValue = "") verificationCode: String
    ): ResponseEntity<MemberRegistrationResponse> {
        return try {
            val request = MemberRegistrationRequest(
                email = email,
                password = password,
                companyName = companyName,
                businessRegistrationNumber = businessRegistrationNumber,
                contactNumber = contactNumber,
                businessField = businessField,
                productDescription = productDescription,
                companyDescription = companyDescription,
                businessRegistrationFile = businessRegistrationFile,
                telecommunicationSalesFile = telecommunicationSalesFile,
                advertisingRegistrationFile = advertisingRegistrationFile,
                verificationCode = verificationCode
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
                    rentalStatus = RentalStatus.INACTIVE,
                    currentRentalExpiry = null,
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
                    rentalStatus = RentalStatus.INACTIVE,
                    currentRentalExpiry = null,
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

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val response = memberService.login(request)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
        }
    }

    @GetMapping("/active")
    fun getActiveMembers(): ResponseEntity<List<Map<String, Any?>>> {
        return try {
            val activeMembers = memberService.getActiveMembers()
            val response = activeMembers.map { member ->
                mapOf(
                    "id" to member.id,
                    "email" to member.email,
                    "companyName" to member.companyName,
                    "rentalStatus" to member.rentalStatus.toString(),
                    "currentRentalExpiry" to member.currentRentalExpiry?.toString()
                )
            }
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        }
    }

    @PostMapping("/complete-registration")
    fun completeRegistration(
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam companyName: String,
        @RequestParam businessRegistrationNumber: String,
        @RequestParam contactNumber: String,
        @RequestParam businessField: String,
        @RequestParam productDescription: String,
        @RequestParam companyDescription: String,
        @RequestParam businessRegistrationFile: MultipartFile,
        @RequestParam telecommunicationSalesFile: MultipartFile,
        @RequestParam advertisingRegistrationFile: MultipartFile,
        @RequestParam rentalContractAgreed: Boolean,
        @RequestParam serviceContractAgreed: Boolean,
        @RequestParam marketingAgreed: Boolean,
        @RequestParam(defaultValue = "1") durationYears: Int
    ): ResponseEntity<CompleteRegistrationResponse> {
        return try {
            val request = CompleteRegistrationRequest(
                email = email,
                password = password,
                companyName = companyName,
                businessRegistrationNumber = businessRegistrationNumber,
                contactNumber = contactNumber,
                businessField = businessField,
                productDescription = productDescription,
                companyDescription = companyDescription,
                businessRegistrationFile = businessRegistrationFile,
                telecommunicationSalesFile = telecommunicationSalesFile,
                advertisingRegistrationFile = advertisingRegistrationFile,
                rentalContractAgreed = rentalContractAgreed,
                serviceContractAgreed = serviceContractAgreed,
                marketingAgreed = marketingAgreed,
                durationYears = durationYears
            )

            val response = memberService.completeRegistration(request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                CompleteRegistrationResponse(
                    success = false,
                    message = e.message ?: "잘못된 요청입니다.",
                    email = email,
                    companyName = companyName,
                    businessRegistrationNumber = businessRegistrationNumber,
                    contactNumber = contactNumber
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                CompleteRegistrationResponse(
                    success = false,
                    message = "서버 오류가 발생했습니다.",
                    email = email,
                    companyName = companyName,
                    businessRegistrationNumber = businessRegistrationNumber,
                    contactNumber = contactNumber
                )
            )
        }
    }

    /**
     * 이메일 인증 링크 발송
     */
    @PostMapping("/email/send-verification")
    fun sendEmailVerification(@RequestBody request: EmailVerificationRequest): ResponseEntity<EmailVerificationResponse> {
        val response = emailVerificationService.sendVerificationLink(request)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }

    /**
     * 이메일 인증 (클릭 기반)
     */
    @GetMapping("/email/verify")
    fun verifyEmailToken(@RequestParam token: String): ResponseEntity<String> {
        val response = emailVerificationService.verifyToken(token)
        return if (response.success) {
            ResponseEntity.ok().body(
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>이메일 인증 완료</title>
                    <style>
                        body { 
                            font-family: Arial, sans-serif; 
                            text-align: center; 
                            padding: 50px;
                            background-color: #f5f5f5;
                        }
                        .container {
                            background: white;
                            padding: 40px;
                            border-radius: 10px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                            display: inline-block;
                        }
                        .success {
                            color: #28a745;
                            font-size: 24px;
                            margin-bottom: 20px;
                        }
                        .email {
                            color: #6c757d;
                            font-size: 16px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="success">✓ 이메일 인증이 완료되었습니다!</div>
                        <div class="email">${response.email}</div>
                        <p>이제 회원가입을 진행하실 수 있습니다.</p>
                    </div>
                </body>
                </html>
                """.trimIndent()
            )
        } else {
            ResponseEntity.badRequest().body(
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>인증 실패</title>
                    <style>
                        body { 
                            font-family: Arial, sans-serif; 
                            text-align: center; 
                            padding: 50px;
                            background-color: #f5f5f5;
                        }
                        .container {
                            background: white;
                            padding: 40px;
                            border-radius: 10px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                            display: inline-block;
                        }
                        .error {
                            color: #dc3545;
                            font-size: 24px;
                            margin-bottom: 20px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="error">✗ 인증에 실패했습니다</div>
                        <p>${response.message}</p>
                    </div>
                </body>
                </html>
                """.trimIndent()
            )
        }
    }

    /**
     * 이메일 인증 상태 확인
     */
    @GetMapping("/email/verification-status")
    fun checkEmailVerificationStatus(@RequestParam email: String): ResponseEntity<Map<String, Any>> {
        val isVerified = emailVerificationService.isEmailVerified(email)
        return ResponseEntity.ok(mapOf(
            "email" to email,
            "isVerified" to isVerified,
            "message" to if (isVerified) "이메일 인증 완료" else "이메일 인증 필요"
        ))
    }
}