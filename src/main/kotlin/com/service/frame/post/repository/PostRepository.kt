package com.service.frame.post.repository

import com.service.frame.post.entity.Post
import com.service.frame.post.entity.PostStatus
import com.service.frame.post.entity.Assignment
import com.service.frame.member.entity.Member
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PostRepository : JpaRepository<Post, Long> {
    
    fun findByStatusOrderByCreatedAtDesc(status: PostStatus, pageable: Pageable): Page<Post>
    
    @Query("SELECT p FROM Post p JOIN Assignment a ON p.assignmentId = a.id WHERE a.publisherMember = :author AND p.status = :status ORDER BY p.createdAt DESC")
    fun findByAuthorAndStatusOrderByCreatedAtDesc(@Param("author") author: Member, @Param("status") status: PostStatus, pageable: Pageable): Page<Post>
    
    @Query("SELECT p FROM Post p JOIN Assignment a ON p.assignmentId = a.id WHERE a.publisherMember = :author ORDER BY p.createdAt DESC")
    fun findByAuthorOrderByCreatedAtDesc(@Param("author") author: Member, pageable: Pageable): Page<Post>
    
    @Query("SELECT p FROM Post p WHERE p.status = :status AND p.content LIKE %:keyword% ORDER BY p.createdAt DESC")
    fun findByStatusAndKeywordOrderByCreatedAtDesc(@Param("status") status: PostStatus, @Param("keyword") keyword: String, pageable: Pageable): Page<Post>
    
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' ORDER BY p.createdAt DESC")
    fun findFeaturedPosts(@Param("now") now: LocalDateTime, pageable: Pageable): Page<Post>
    
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' ORDER BY p.finalRevenue DESC")
    fun findPopularPosts(pageable: Pageable): Page<Post>
    
    @Query("SELECT COUNT(p) FROM Post p JOIN Assignment a ON p.assignmentId = a.id WHERE a.publisherMember = :author AND p.status = :status")
    fun countByAuthorAndStatus(@Param("author") author: Member, @Param("status") status: PostStatus): Long
    
    fun countByStatus(status: PostStatus): Long
    
    fun findByAssignmentId(assignmentId: Long): Post?
    
    @Query("SELECT p FROM Post p JOIN Assignment a ON p.assignmentId = a.id WHERE a.publisherMember = :author AND a.adTask.id = :targetAdTaskId")
    fun findByAuthorAndTargetAdTaskId(@Param("author") author: Member, @Param("targetAdTaskId") targetAdTaskId: Long): Post?
}