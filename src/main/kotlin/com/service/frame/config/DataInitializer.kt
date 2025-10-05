package com.service.frame.config

import com.service.frame.member.entity.Member
import com.service.frame.member.repository.MemberRepository
import com.service.frame.round.entity.Round
import com.service.frame.round.entity.RoundStatus
import com.service.frame.round.repository.RoundRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class DataInitializer(
    private val memberRepository: MemberRepository,
    private val roundRepository: RoundRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    @Transactional
    override fun run(vararg args: String?) {
        if (memberRepository.count() == 0L) {
            createSampleMembers()
        }
        
        if (roundRepository.count() == 0L) {
            createSampleRounds()
        }
    }

    private fun createSampleMembers() {
        val adminMember = Member(
            email = "admin@example.com",
            password = passwordEncoder.encode("admin123"),
            companyName = "관리자 회사",
            businessRegistrationNumber = "123-45-67890",
            contactNumber = "010-1234-5678",
            businessRegistrationFile = "/uploads/admin_business.pdf",
            telecommunicationSalesFile = "/uploads/admin_telecom.pdf",
            isPremium = true
        )
        
        memberRepository.save(adminMember)
        logger.info("Created admin member: ${adminMember.email}")
    }

    private fun createSampleRounds() {
        val adminMember = memberRepository.findByEmail("admin@example.com")
            ?: throw IllegalStateException("Admin member not found")

        val koreaTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))

        // 1. 완료된 라운드 (종료일이 과거)
        val completedRound = Round(
            roundNumber = "Round #1",
            title = "헬스케어 광고 라운드 #1",
            description = "헬스케어 분야 제품 광고를 위한 첫 번째 라운드입니다. 건강보조식품, 의료기기, 헬스케어 앱 등의 광고를 진행합니다.",
            category = "헬스케어",
            orderAmount = BigDecimal("150000.00"),
            startDate = koreaTime.minusDays(30), // 30일 전 시작
            endDate = koreaTime.minusDays(1),   // 1일 전 종료
            status = RoundStatus.CLOSED,
            maxParticipants = 20,
            createdBy = adminMember
        )

        // 2. 모집 중인 라운드 (종료일이 미래)
        val activeRound = Round(
            roundNumber = "Round #2",
            title = "뷰티 & 코스메틱 라운드 #2",
            description = "뷰티 및 코스메틱 제품 광고를 위한 두 번째 라운드입니다. 스킨케어, 메이크업, 헤어케어 제품의 광고를 진행합니다.",
            category = "뷰티",
            orderAmount = BigDecimal("200000.00"),
            startDate = koreaTime.minusHours(1), // 1시간 전 시작
            endDate = koreaTime.plusDays(30),    // 30일 후 종료
            status = RoundStatus.ACTIVE,
            maxParticipants = 30,
            createdBy = adminMember
        )

        roundRepository.saveAll(listOf(completedRound, activeRound))
        
        logger.info("Created sample rounds:")
        logger.info("- Completed round: ${completedRound.title} (${completedRound.status})")
        logger.info("- Active round: ${activeRound.title} (${activeRound.status})")
    }
}