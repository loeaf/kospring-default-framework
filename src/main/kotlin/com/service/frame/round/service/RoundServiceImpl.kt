package com.service.frame.round.service

import com.service.frame.member.repository.MemberRepository
import com.service.frame.round.dto.RoundCreateRequest
import com.service.frame.round.dto.RoundResponse
import com.service.frame.round.entity.Round
import com.service.frame.round.entity.RoundStatus
import com.service.frame.round.repository.RoundRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Service
@Transactional
class RoundServiceImpl(
    private val roundRepository: RoundRepository,
    private val memberRepository: MemberRepository,
    @PersistenceContext private val entityManager: EntityManager
) : RoundService {

    override fun createRound(request: RoundCreateRequest, createdById: Long): RoundResponse {
        val creator = memberRepository.findByIdOrNull(createdById) 
            ?: throw IllegalArgumentException("Member not found with id: $createdById")

        val round = Round(
            title = request.title,
            description = request.description,
            category = request.category,
            orderAmount = request.orderAmount,
            startDate = request.startDate,
            endDate = request.endDate,
            maxParticipants = request.maxParticipants,
            createdBy = creator
        )

        val savedRound = roundRepository.save(round)
        
        // 라운드 번호 생성 (DB 함수 사용)
        val roundNumber = generateRoundNumber()
        savedRound.roundNumber = roundNumber
        
        return RoundResponse.from(savedRound)
    }

    @Transactional(readOnly = true)
    override fun getRounds(status: RoundStatus?, pageable: Pageable): Page<RoundResponse> {
        val rounds = if (status != null) {
            roundRepository.findActiveRoundsOrderByCreatedAtDesc(status, pageable)
        } else {
            roundRepository.findAll(pageable)
        }
        
        return rounds.map { RoundResponse.from(it) }
    }

    @Transactional(readOnly = true)
    override fun getRoundById(id: Long): RoundResponse {
        val round = roundRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("Round not found with id: $id")
        
        return RoundResponse.from(round)
    }

    @Transactional(readOnly = true)
    override fun getRoundsByCreatedBy(memberId: Long, pageable: Pageable): Page<RoundResponse> {
        val rounds = roundRepository.findByCreatedByIdOrderByCreatedAtDesc(memberId, pageable)
        return rounds.map { RoundResponse.from(it) }
    }

    private fun generateRoundNumber(): String {
        val query = entityManager.createNativeQuery("SELECT generate_round_number()")
        return query.singleResult as String
    }
}