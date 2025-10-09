package com.service.frame.member.service

import com.service.frame.member.dto.RentalRightsRequest
import com.service.frame.member.dto.RentalRightsResponse
import com.service.frame.member.dto.ContractRequest
import com.service.frame.member.entity.RentalRights
import com.service.frame.member.entity.RentalRightsStatus
import com.service.frame.member.entity.RentalStatus
import com.service.frame.member.repository.MemberRepository
import com.service.frame.member.repository.RentalRightsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
@Transactional
class RentalRightsService(
    private val memberRepository: MemberRepository,
    private val rentalRightsRepository: RentalRightsRepository,
    private val contractService: ContractService
) {

    fun purchaseRentalRights(request: RentalRightsRequest): RentalRightsResponse {
        val member = memberRepository.findById(request.memberId).orElseThrow {
            IllegalArgumentException("회원을 찾을 수 없습니다.")
        }

        val expiryDate = request.purchaseDate.plusYears(request.durationYears.toLong())

        val rentalRights = RentalRights(
            member = member,
            purchaseDate = request.purchaseDate,
            expiryDate = expiryDate,
            rentalAmount = BigDecimal("100000000"), // 1억원 고정
            status = RentalRightsStatus.ACTIVE,
            autoRenewal = false,
            renewalNoticeSent = false
        )

        val savedRentalRights = rentalRightsRepository.save(rentalRights)

        // 서비스 계약이 없는 경우에만 자동 생성 (임대권 구매 시 필수 계약 동의로 간주)
        val hasExistingContract = contractService.hasValidContract(member.id!!)
        if (!hasExistingContract) {
            val contractRequest = ContractRequest(
                memberId = member.id!!,
                rentalContractAgreed = true,
                serviceContractAgreed = true,
                marketingAgreed = false,
                ipAddress = "system",
                userAgent = "auto-generated"
            )
            contractService.createContract(contractRequest)
        }

        // 회원의 임대권 상태 업데이트
        val updatedMember = member.copy(
            isPremium = true,
            rentalStatus = RentalStatus.ACTIVE,
            currentRentalExpiry = expiryDate
        )
        memberRepository.save(updatedMember)

        return RentalRightsResponse(
            id = savedRentalRights.id!!,
            memberId = savedRentalRights.member.id!!,
            purchaseDate = savedRentalRights.purchaseDate,
            expiryDate = savedRentalRights.expiryDate,
            rentalAmount = savedRentalRights.rentalAmount,
            status = savedRentalRights.status,
            autoRenewal = savedRentalRights.autoRenewal,
            renewalNoticeSent = savedRentalRights.renewalNoticeSent,
            message = "임대권이 성공적으로 구매되었습니다.",
            success = true
        )
    }

    @Transactional(readOnly = true)
    fun getActiveRentalRights(memberId: Long): List<RentalRightsResponse> {
        val member = memberRepository.findById(memberId).orElseThrow {
            IllegalArgumentException("회원을 찾을 수 없습니다.")
        }

        val activeRentalRights = rentalRightsRepository.findActiveRentalRights(member)

        return activeRentalRights.map { rentalRights ->
            RentalRightsResponse(
                id = rentalRights.id!!,
                memberId = rentalRights.member.id!!,
                purchaseDate = rentalRights.purchaseDate,
                expiryDate = rentalRights.expiryDate,
                rentalAmount = rentalRights.rentalAmount,
                status = rentalRights.status,
                autoRenewal = rentalRights.autoRenewal,
                renewalNoticeSent = rentalRights.renewalNoticeSent,
                message = "활성화된 임대권입니다.",
                success = true
            )
        }
    }

    @Transactional(readOnly = true)
    fun hasValidRentalRights(memberId: Long): Boolean {
        val member = memberRepository.findById(memberId).orElse(null)
            ?: return false

        val activeRentalRights = rentalRightsRepository.findActiveRentalRights(member)
        return activeRentalRights.isNotEmpty()
    }

    fun renewRentalRights(rentalRightsId: Long, durationYears: Int = 1): RentalRightsResponse {
        val existingRentalRights = rentalRightsRepository.findById(rentalRightsId).orElseThrow {
            IllegalArgumentException("임대권을 찾을 수 없습니다.")
        }

        // 기존 임대권 만료 처리
        val expiredRentalRights = existingRentalRights.copy(status = RentalRightsStatus.EXPIRED)
        rentalRightsRepository.save(expiredRentalRights)

        // 새로운 임대권 생성
        val newExpiryDate = LocalDate.now().plusYears(durationYears.toLong())
        val newRentalRights = RentalRights(
            member = existingRentalRights.member,
            purchaseDate = LocalDate.now(),
            expiryDate = newExpiryDate,
            rentalAmount = BigDecimal("100000000"),
            status = RentalRightsStatus.ACTIVE,
            autoRenewal = existingRentalRights.autoRenewal,
            renewalNoticeSent = false
        )

        val savedRentalRights = rentalRightsRepository.save(newRentalRights)

        // 서비스 계약 갱신 (기존 계약이 있다면 새로운 계약 생성)
        val contractRequest = ContractRequest(
            memberId = existingRentalRights.member.id!!,
            rentalContractAgreed = true,
            serviceContractAgreed = true,
            marketingAgreed = false,
            ipAddress = "system-renewal",
            userAgent = "auto-renewal"
        )
        contractService.createContract(contractRequest)

        // 회원의 임대권 상태 업데이트
        val updatedMember = existingRentalRights.member.copy(
            isPremium = true,
            rentalStatus = RentalStatus.ACTIVE,
            currentRentalExpiry = newExpiryDate
        )
        memberRepository.save(updatedMember)

        return RentalRightsResponse(
            id = savedRentalRights.id!!,
            memberId = savedRentalRights.member.id!!,
            purchaseDate = savedRentalRights.purchaseDate,
            expiryDate = savedRentalRights.expiryDate,
            rentalAmount = savedRentalRights.rentalAmount,
            status = savedRentalRights.status,
            autoRenewal = savedRentalRights.autoRenewal,
            renewalNoticeSent = savedRentalRights.renewalNoticeSent,
            message = "임대권이 성공적으로 갱신되었습니다.",
            success = true
        )
    }

    @Transactional(readOnly = true)
    fun getExpiringRentalRights(days: Int = 30): List<RentalRightsResponse> {
        val today = LocalDate.now()
        val expiryDate = today.plusDays(days.toLong())
        
        val expiringRentalRights = rentalRightsRepository.findExpiringRentalRights(today, expiryDate)
        
        return expiringRentalRights.map { rentalRights ->
            RentalRightsResponse(
                id = rentalRights.id!!,
                memberId = rentalRights.member.id!!,
                purchaseDate = rentalRights.purchaseDate,
                expiryDate = rentalRights.expiryDate,
                rentalAmount = rentalRights.rentalAmount,
                status = rentalRights.status,
                autoRenewal = rentalRights.autoRenewal,
                renewalNoticeSent = rentalRights.renewalNoticeSent,
                message = "만료 예정인 임대권입니다.",
                success = true
            )
        }
    }
}