package com.service.frame.member.controller

import com.service.frame.member.dto.ContractRequest
import com.service.frame.member.dto.ContractResponse
import com.service.frame.member.service.ContractService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/contracts")
class ContractController(
    private val contractService: ContractService
) {

    @PostMapping("/create")
    fun createContract(
        @RequestBody request: ContractRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ContractResponse> {
        
        // IP 주소와 User-Agent 추가
        val enrichedRequest = request.copy(
            ipAddress = httpRequest.remoteAddr,
            userAgent = httpRequest.getHeader("User-Agent")
        )
        
        val response = contractService.createContract(enrichedRequest)
        
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }

    @GetMapping("/member/{memberId}/active")
    fun getActiveContract(@PathVariable memberId: Long): ResponseEntity<ContractResponse> {
        val contract = contractService.getActiveContract(memberId)
        
        return if (contract != null) {
            ResponseEntity.ok(contract)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/member/{memberId}/check")
    fun checkValidContract(@PathVariable memberId: Long): ResponseEntity<Map<String, Boolean>> {
        val hasValidContract = contractService.hasValidContract(memberId)
        
        return ResponseEntity.ok(mapOf("hasValidContract" to hasValidContract))
    }
}