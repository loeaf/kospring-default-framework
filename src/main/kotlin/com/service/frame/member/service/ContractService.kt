package com.service.frame.member.service

import com.service.frame.member.dto.ContractRequest
import com.service.frame.member.dto.ContractResponse
import com.service.frame.member.entity.ServiceContract
import com.service.frame.member.entity.RentalStatus
import com.service.frame.member.repository.MemberRepository
import com.service.frame.member.repository.ServiceContractRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Service
@Transactional
class ContractService(
    private val memberRepository: MemberRepository,
    private val serviceContractRepository: ServiceContractRepository
) {
    private val logger = LoggerFactory.getLogger(ContractService::class.java)

    fun createContract(request: ContractRequest): ContractResponse {
        val member = memberRepository.findById(request.memberId).orElseThrow {
            IllegalArgumentException("회원을 찾을 수 없습니다.")
        }

        // 필수 계약 동의 체크
        if (!request.rentalContractAgreed || !request.serviceContractAgreed) {
            return ContractResponse(
                id = 0,
                memberId = request.memberId,
                rentalContractAgreed = request.rentalContractAgreed,
                serviceContractAgreed = request.serviceContractAgreed,
                contractDate = LocalDateTime.now(),
                contractVersion = "1.0",
                isActive = false,
                message = "필수 계약에 모두 동의해주세요.",
                success = false
            )
        }

        // 기존 활성화된 계약이 있으면 비활성화
        val existingContract = serviceContractRepository.findByMemberAndIsActive(member, true)
        existingContract?.let {
            val deactivatedContract = it.copy(isActive = false)
            serviceContractRepository.save(deactivatedContract)
        }

        // 새 계약 생성
        val newContract = ServiceContract(
            member = member,
            rentalContractAgreed = request.rentalContractAgreed,
            serviceContractAgreed = request.serviceContractAgreed,
            marketingAgreed = false,
            contractDate = LocalDateTime.now(),
            contractVersion = "1.0",
            ipAddress = request.ipAddress,
            userAgent = request.userAgent,
            isActive = true
        )

        val savedContract = serviceContractRepository.save(newContract)

        return ContractResponse(
            id = savedContract.id!!,
            memberId = savedContract.member.id!!,
            rentalContractAgreed = savedContract.rentalContractAgreed,
            serviceContractAgreed = savedContract.serviceContractAgreed,
            contractDate = savedContract.contractDate,
            contractVersion = savedContract.contractVersion,
            isActive = savedContract.isActive,
            message = "계약이 성공적으로 체결되었습니다.",
            success = true
        )
    }

    @Transactional(readOnly = true)
    fun getActiveContract(memberId: Long): ContractResponse? {
        val member = memberRepository.findById(memberId).orElse(null)
            ?: return null

        val contract = serviceContractRepository.findByMemberAndIsActive(member, true)
            ?: return null

        return ContractResponse(
            id = contract.id!!,
            memberId = contract.member.id!!,
            rentalContractAgreed = contract.rentalContractAgreed,
            serviceContractAgreed = contract.serviceContractAgreed,
            contractDate = contract.contractDate,
            contractVersion = contract.contractVersion,
            isActive = contract.isActive,
            message = "활성화된 계약이 존재합니다.",
            success = true
        )
    }

    @Transactional(readOnly = true)
    fun hasValidContract(memberId: Long): Boolean {
        logger.info("=== hasValidContract 호출 ===")
        logger.info("memberId: {}", memberId)
        
        val member = memberRepository.findById(memberId).orElse(null)
        if (member == null) {
            logger.warn("회원을 찾을 수 없습니다. memberId: {}", memberId)
            return false
        }
        
        logger.info("회원 정보 - id: {}, email: {}", member.id, member.email)
        
        val hasContract = serviceContractRepository.existsByMemberAndIsActive(member, true)
        logger.info("계약 존재 여부: {}", hasContract)
        
        // 추가로 모든 계약 조회해서 로그 출력
        val allContracts = serviceContractRepository.findAllByMember(member)
        logger.info("해당 회원의 모든 계약 수: {}", allContracts.size)
        allContracts.forEach { contract ->
            logger.info("계약 ID: {}, isActive: {}, 생성일: {}", contract.id, contract.isActive, contract.contractDate)
        }
        
        return hasContract
    }
}