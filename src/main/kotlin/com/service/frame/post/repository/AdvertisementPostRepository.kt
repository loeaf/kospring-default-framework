package com.service.frame.post.repository

import com.service.frame.post.entity.AdvertisementPost
import com.service.frame.post.entity.AdvertisementPostStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AdvertisementPostRepository : JpaRepository<AdvertisementPost, Long> {
    
    fun findByAssignmentId(assignmentId: Long): AdvertisementPost?
    
    @Query("""
        SELECT ap FROM AdvertisementPost ap 
        JOIN ap.assignment aa 
        WHERE aa.publisherMember.id = :publisherId 
        ORDER BY ap.submittedAt DESC
    """)
    fun findByPublisherMemberIdOrderBySubmittedAtDesc(
        @Param("publisherId") publisherId: Long
    ): List<AdvertisementPost>
    
    @Query("""
        SELECT ap FROM AdvertisementPost ap 
        JOIN ap.assignment aa 
        WHERE aa.publisherMember.id = :publisherId 
        AND aa.round.id = :roundId 
        ORDER BY ap.submittedAt DESC
    """)
    fun findByPublisherMemberIdAndRoundIdOrderBySubmittedAtDesc(
        @Param("publisherId") publisherId: Long,
        @Param("roundId") roundId: Long
    ): List<AdvertisementPost>
    
    @Query("""
        SELECT ap FROM AdvertisementPost ap 
        JOIN ap.assignment aa 
        WHERE aa.advertiserMember.id = :advertiserId 
        ORDER BY ap.submittedAt DESC
    """)
    fun findByAdvertiserMemberIdOrderBySubmittedAtDesc(
        @Param("advertiserId") advertiserId: Long
    ): List<AdvertisementPost>
    
    fun findByPostStatusOrderBySubmittedAtDesc(postStatus: AdvertisementPostStatus): List<AdvertisementPost>
    
    @Query("""
        SELECT COUNT(ap) FROM AdvertisementPost ap 
        JOIN ap.assignment aa 
        WHERE aa.publisherMember.id = :publisherId 
        AND ap.postStatus = :status
    """)
    fun countByPublisherMemberIdAndPostStatus(
        @Param("publisherId") publisherId: Long,
        @Param("status") status: AdvertisementPostStatus
    ): Long
}