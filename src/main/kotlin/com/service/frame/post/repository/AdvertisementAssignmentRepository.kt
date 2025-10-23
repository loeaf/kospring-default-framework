package com.service.frame.post.repository

import com.service.frame.post.entity.AdvertisementAssignment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AdvertisementAssignmentRepository : JpaRepository<AdvertisementAssignment, Long> {
    
    fun findByPublisherMemberIdOrderByCreatedAtDesc(publisherMemberId: Long): List<AdvertisementAssignment>
    
    fun findByPublisherMemberIdAndRoundIdOrderByCreatedAtDesc(
        publisherMemberId: Long, 
        roundId: Long
    ): List<AdvertisementAssignment>
    
    fun findByAdvertiserMemberIdOrderByCreatedAtDesc(advertiserMemberId: Long): List<AdvertisementAssignment>
    
    fun findByRoundIdOrderByCreatedAtDesc(roundId: Long): List<AdvertisementAssignment>
    
    @Query("""
        SELECT aa FROM AdvertisementAssignment aa 
        WHERE aa.publisherMember.id = :publisherId 
        AND aa.id = :assignmentId
    """)
    fun findByPublisherMemberIdAndId(
        @Param("publisherId") publisherId: Long,
        @Param("assignmentId") assignmentId: Long
    ): AdvertisementAssignment?
    
    fun findByAdTaskId(adTaskId: Long): List<AdvertisementAssignment>
}