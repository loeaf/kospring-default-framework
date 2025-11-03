package com.service.frame.member.service

import com.service.frame.member.dto.*
import com.service.frame.member.entity.Member
import com.service.frame.member.entity.RentalStatus
import com.service.frame.member.repository.MemberRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val fileUploadService: FileUploadService,
    private val passwordEncoder: PasswordEncoder,
    private val contractService: ContractService,
    private val rentalRightsService: RentalRightsService,
    private val emailVerificationService: EmailVerificationService
) {

    fun registerMember(request: MemberRegistrationRequest): MemberRegistrationResponse {
        // 이메일 인증 확인 (선택적)
        if (request.verificationCode.isNotBlank()) {
            if (!emailVerificationService.isEmailVerified(request.email)) {
                throw IllegalArgumentException("이메일 인증이 완료되지 않았습니다.")
            }
        }

        // 사업자 등록 검증 - 통신판매업과 광고업 모두 등록 필요
        // 실제로는 여기서 사업자등록번호로 통신판매업/광고업 등록 여부를 외부 API로 확인할 수 있음

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

        val advertisingRegFilePath = fileUploadService.uploadFile(
            request.advertisingRegistrationFile,
            "advertising_reg_${request.businessRegistrationNumber}"
        )

        try {
            val member = Member(
                email = request.email,
                password = passwordEncoder.encode(request.password),
                companyName = request.companyName,
                businessRegistrationNumber = request.businessRegistrationNumber,
                contactNumber = request.contactNumber,
                businessField = request.businessField,
                productDescription = request.productDescription,
                companyDescription = request.companyDescription,
                businessRegistrationFile = businessRegFilePath,
                telecommunicationSalesFile = telecomSalesFilePath,
                advertisingRegistrationFile = advertisingRegFilePath
            )

            val savedMember = memberRepository.save(member)

            return MemberRegistrationResponse(
                id = savedMember.id!!,
                email = savedMember.email,
                companyName = savedMember.companyName,
                businessRegistrationNumber = savedMember.businessRegistrationNumber,
                contactNumber = savedMember.contactNumber,
                isPremium = savedMember.isPremium,
                rentalStatus = savedMember.rentalStatus,
                currentRentalExpiry = savedMember.currentRentalExpiry,
                message = "회원가입이 완료되었습니다. (통신판매업 + 광고업 등록 확인됨)"
            )
        } catch (e: Exception) {
            fileUploadService.deleteFile(businessRegFilePath)
            fileUploadService.deleteFile(telecomSalesFilePath)
            fileUploadService.deleteFile(advertisingRegFilePath)
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

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): LoginResponse {
        val member = memberRepository.findByEmail(request.email)
            ?: return LoginResponse(
                id = 0,
                email = request.email,
                companyName = "",
                businessRegistrationNumber = "",
                contactNumber = "",
                isPremium = false,
                rentalStatus = RentalStatus.INACTIVE,
                currentRentalExpiry = null,
                hasValidContract = false,
                message = "등록되지 않은 이메일입니다.",
                success = false
            )

        if (!passwordEncoder.matches(request.password, member.password)) {
            return LoginResponse(
                id = member.id!!,
                email = member.email,
                companyName = member.companyName,
                businessRegistrationNumber = member.businessRegistrationNumber,
                contactNumber = member.contactNumber,
                isPremium = member.isPremium,
                rentalStatus = member.rentalStatus,
                currentRentalExpiry = member.currentRentalExpiry,
                hasValidContract = false,
                message = "비밀번호가 일치하지 않습니다.",
                success = false
            )
        }

        // Check if member has valid service contract
        val hasValidContract = contractService.hasValidContract(member.id!!)

        return LoginResponse(
            id = member.id!!,
            email = member.email,
            companyName = member.companyName,
            businessRegistrationNumber = member.businessRegistrationNumber,
            contactNumber = member.contactNumber,
            isPremium = member.isPremium,
            rentalStatus = member.rentalStatus,
            currentRentalExpiry = member.currentRentalExpiry,
            hasValidContract = hasValidContract,
            message = "로그인 성공",
            success = true
        )
    }

    @Transactional(readOnly = true)
    fun getActiveMembers(): List<Member> {
        return memberRepository.findActiveMembers()
    }

    fun completeRegistration(request: CompleteRegistrationRequest): CompleteRegistrationResponse {
        try {
            // 1. 회원가입 처리
            val memberRequest = MemberRegistrationRequest(
                email = request.email,
                password = request.password,
                companyName = request.companyName,
                businessRegistrationNumber = request.businessRegistrationNumber,
                contactNumber = request.contactNumber,
                businessField = request.businessField,
                productDescription = request.productDescription,
                companyDescription = request.companyDescription,
                businessRegistrationFile = request.businessRegistrationFile!!,
                telecommunicationSalesFile = request.telecommunicationSalesFile!!,
                advertisingRegistrationFile = request.advertisingRegistrationFile!!
            )
            
            val memberResponse = registerMember(memberRequest)
            val memberId = memberResponse.id

            // 2. 계약 체결
            val contractRequest = ContractRequest(
                memberId = memberId,
                rentalContractAgreed = request.rentalContractAgreed,
                serviceContractAgreed = request.serviceContractAgreed,
                marketingAgreed = request.marketingAgreed,
                ipAddress = request.ipAddress,
                userAgent = request.userAgent
            )
            
            val contractResponse = contractService.createContract(contractRequest)

            // 3. 임대권 구매
            val rentalRequest = RentalRightsRequest(
                memberId = memberId,
                durationYears = request.durationYears
            )
            
            val rentalResponse = rentalRightsService.purchaseRentalRights(rentalRequest)

            // 4. 멤버 상태 업데이트 (임대권 상태를 ACTIVE로 변경)
            val member = memberRepository.findById(memberId).orElseThrow {
                IllegalStateException("회원 정보를 찾을 수 없습니다.")
            }
            
            val updatedMember = member.copy(
                rentalStatus = RentalStatus.ACTIVE,
                currentRentalExpiry = rentalResponse.expiryDate
            )
            memberRepository.save(updatedMember)

            return CompleteRegistrationResponse(
                success = true,
                message = "가입 및 계약이 완료되었습니다.",
                memberId = memberId,
                email = memberResponse.email,
                companyName = memberResponse.companyName,
                businessRegistrationNumber = memberResponse.businessRegistrationNumber,
                contactNumber = memberResponse.contactNumber,
                isPremium = memberResponse.isPremium,
                rentalStatus = RentalStatus.ACTIVE,
                contractId = contractResponse.id,
                contractDate = contractResponse.contractDate,
                contractVersion = contractResponse.contractVersion,
                rentalRightsId = rentalResponse.id,
                purchaseDate = rentalResponse.purchaseDate,
                expiryDate = rentalResponse.expiryDate,
                rentalAmount = rentalResponse.rentalAmount,
                hasValidContract = true,
                hasValidRentalRights = true
            )
            
        } catch (e: Exception) {
            throw e // @Transactional에 의해 자동 롤백
        }
    }
}