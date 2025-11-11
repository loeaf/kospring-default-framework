package com.service.frame.round.repository

import com.service.frame.round.entity.Round
import com.service.frame.round.entity.RoundStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RoundRepository : JpaRepository<Round, Long> {
    
    fun findByStatus(status: RoundStatus, pageable: Pageable): Page<Round>
    
    fun findByRoundNumber(roundNumber: String): Round?
    
    @Query("SELECT r FROM Round r WHERE r.status = :status ORDER BY r.createdAt DESC")
    fun findActiveRoundsOrderByCreatedAtDesc(status: RoundStatus, pageable: Pageable): Page<Round>
    
    @Query("SELECT r FROM Round r WHERE r.createdBy.id = :memberId ORDER BY r.createdAt DESC")
    fun findByCreatedByIdOrderByCreatedAtDesc(memberId: Long, pageable: Pageable): Page<Round>
    
    @Query("SELECT r FROM Round r ORDER BY r.createdAt DESC")
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<Round>
    
    @Query("SELECT r FROM Round r WHERE r.status = 'ACTIVE' AND r.endDate <= :currentTime")
    fun findExpiredActiveRounds(currentTime: LocalDateTime): List<Round>
    
    @Modifying
    @Query("UPDATE Round r SET r.status = 'CLOSED' WHERE r.status = 'ACTIVE' AND r.endDate <= :currentTime")
    fun updateExpiredRoundsToClose(currentTime: LocalDateTime): Int
}