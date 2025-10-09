package com.service.frame.ad.repository

import com.service.frame.ad.entity.AdTask
import com.service.frame.ad.entity.AdTaskStatus
import com.service.frame.member.entity.Member
import com.service.frame.round.entity.Round
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AdTaskRepository : JpaRepository<AdTask, Long> {
    
    fun findByRoundAndMember(round: Round, member: Member): AdTask?
    
    fun findByRound(round: Round): List<AdTask>
    
    fun findByStatus(status: AdTaskStatus): List<AdTask>
    
    @Query("SELECT at FROM AdTask at WHERE at.status = :status AND at.retryCount < :maxRetry")
    fun findByStatusAndRetryCountLessThan(
        @Param("status") status: AdTaskStatus, 
        @Param("maxRetry") maxRetry: Int
    ): List<AdTask>
    
    @Query("SELECT COUNT(at) FROM AdTask at WHERE at.round = :round")
    fun countByRound(@Param("round") round: Round): Long
    
    @Query("SELECT COUNT(at) FROM AdTask at WHERE at.round = :round AND at.status = :status")
    fun countByRoundAndStatus(
        @Param("round") round: Round, 
        @Param("status") status: AdTaskStatus
    ): Long
    
    fun findByRoundAndStatus(round: Round, status: AdTaskStatus): List<AdTask>
    
    // 라운드별 완료된 광고 조회 (HTML 콘텐츠가 있는 것만)
    @Query("SELECT at FROM AdTask at WHERE at.round.id = :roundId AND at.status = 'COMPLETED' AND at.adContent IS NOT NULL")
    fun findCompletedAdsByRoundId(@Param("roundId") roundId: Long): List<AdTask>
    
    // 특정 회원의 모든 광고 작업 조회
    @Query("SELECT at FROM AdTask at WHERE at.member.id = :memberId ORDER BY at.createdAt DESC")
    fun findByMemberId(@Param("memberId") memberId: Long): List<AdTask>
    
    // 특정 회원의 라운드별 광고 작업 조회 (완료된 것만)
    @Query("SELECT at FROM AdTask at WHERE at.round.id = :roundId AND at.member.id = :memberId AND at.status = 'COMPLETED'")
    fun findByRoundIdAndMemberId(@Param("roundId") roundId: Long, @Param("memberId") memberId: Long): List<AdTask>
    
    // 특정 회원의 완료된 광고만 조회
    @Query("SELECT at FROM AdTask at WHERE at.member.id = :memberId AND at.status = 'COMPLETED' AND at.adContent IS NOT NULL ORDER BY at.completedAt DESC")
    fun findCompletedAdsByMemberId(@Param("memberId") memberId: Long): List<AdTask>
}