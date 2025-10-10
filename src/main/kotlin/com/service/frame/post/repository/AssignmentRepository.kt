package com.service.frame.post.repository

import com.service.frame.post.entity.Assignment
import com.service.frame.post.entity.AssignmentStatus
import com.service.frame.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AssignmentRepository : JpaRepository<Assignment, Long> {
    
    fun findByRoundId(roundId: Long): List<Assignment>
    
    fun findByRoundIdAndAssignmentStatus(roundId: Long, status: AssignmentStatus): List<Assignment>
    
    fun findByPublisherMember(publisherMember: Member): List<Assignment>
    
    fun findByAdvertiserMember(advertiserMember: Member): List<Assignment>
    
    @Query("SELECT a FROM Assignment a WHERE a.round.id = :roundId AND a.publisherMember.id = :publisherId AND a.adTask.id = :adId")
    fun findByRoundAndPublisherAndAd(
        @Param("roundId") roundId: Long,
        @Param("publisherId") publisherId: Long, 
        @Param("adId") adId: Long
    ): Assignment?
}