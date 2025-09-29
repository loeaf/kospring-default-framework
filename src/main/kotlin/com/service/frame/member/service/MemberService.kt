package com.service.frame.member.service

import com.service.frame.member.dto.MemberRegistrationRequest
import com.service.frame.member.dto.MemberRegistrationResponse
import com.service.frame.member.dto.ValidationResponse
import com.service.frame.member.entity.Member
import com.service.frame.member.repository.MemberRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val fileUploadService: FileUploadService,
    private val passwordEncoder: PasswordEncoder
) {

    fun registerMember(request: MemberRegistrationRequest): MemberRegistrationResponse {
        if (memberRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 등록된 이메일입니다.")
        }

        if (memberRepository.existsByBusinessRegistrationNumber(request.businessRegistrationNumber)) {
            throw IllegalArgumentException("이미 등록된 사업자등록번호입니다.")
        }

        val businessRegFilePath = fileUploadService.uploadFile(
            request.businessRegistrationFile, 
            "business_reg_${request.businessRegistrationNumber}"
        )

        val telecomSalesFilePath = fileUploadService.uploadFile(
            request.telecommunicationSalesFile, 
            "telecom_sales_${request.businessRegistrationNumber}"
        )

        try {
            val member = Member(
                email = request.email,
                password = passwordEncoder.encode(request.password),
                companyName = request.companyName,
                businessRegistrationNumber = request.businessRegistrationNumber,
                contactNumber = request.contactNumber,
                businessRegistrationFile = businessRegFilePath,
                telecommunicationSalesFile = telecomSalesFilePath
            )

            val savedMember = memberRepository.save(member)

            return MemberRegistrationResponse(
                id = savedMember.id!!,
                email = savedMember.email,
                companyName = savedMember.companyName,
                businessRegistrationNumber = savedMember.businessRegistrationNumber,
                contactNumber = savedMember.contactNumber,
                isPremium = savedMember.isPremium,
                message = "회원가입이 완료되었습니다."
            )
        } catch (e: Exception) {
            fileUploadService.deleteFile(businessRegFilePath)
            fileUploadService.deleteFile(telecomSalesFilePath)
            throw e
        }
    }

    @Transactional(readOnly = true)
    fun checkEmailAvailability(email: String): ValidationResponse {
        val isAvailable = !memberRepository.existsByEmail(email)
        return ValidationResponse(
            isAvailable = isAvailable,
            message = if (isAvailable) "사용 가능한 이메일입니다." else "이미 등록된 이메일입니다."
        )
    }

    @Transactional(readOnly = true)
    fun checkBusinessRegistrationNumberAvailability(businessRegistrationNumber: String): ValidationResponse {
        val isAvailable = !memberRepository.existsByBusinessRegistrationNumber(businessRegistrationNumber)
        return ValidationResponse(
            isAvailable = isAvailable,
            message = if (isAvailable) "사용 가능한 사업자등록번호입니다." else "이미 등록된 사업자등록번호입니다."
        )
    }
}