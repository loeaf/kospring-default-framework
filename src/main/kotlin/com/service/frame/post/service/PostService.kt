package com.service.frame.post.service

import com.service.frame.post.dto.*
import com.service.frame.post.entity.Post
import com.service.frame.post.entity.PostStatus
import com.service.frame.post.repository.PostRepository
import com.service.frame.post.repository.AssignmentRepository
import com.service.frame.post.entity.Assignment
import com.service.frame.post.entity.AssignmentStatus
import com.service.frame.member.entity.Member
import com.service.frame.member.repository.MemberRepository
import com.service.frame.round.entity.Round
import com.service.frame.round.repository.RoundRepository
import com.service.frame.ad.entity.AdTask
import com.service.frame.ad.repository.AdTaskRepository
import com.service.frame.order.repository.OrderRepository
import com.service.frame.order.entity.OrderStatus
import com.service.frame.post.entity.PostType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.math.BigDecimal

@Service
@Transactional(readOnly = true)
class PostService(
    private val postRepository: PostRepository,
    private val assignmentRepository: AssignmentRepository,
    private val memberRepository: MemberRepository,
    private val roundRepository: RoundRepository,
    private val adTaskRepository: AdTaskRepository,
    private val orderRepository: OrderRepository,
    private val circularAssignmentService: CircularAssignmentService
) {

    @Transactional
    fun createPost(authorId: Long, request: PostCreateRequest): PostResponse {
        val author = memberRepository.findById(authorId)
            .orElseThrow { IllegalArgumentException("Member not found: $authorId") }
        
        val post = Post(
            title = request.title,
            content = request.content,
            author = author,
            status = request.status
        )
        
        val savedPost = postRepository.save(post)
        return createPostResponse(savedPost)
    }

    @Transactional
    fun updatePost(postId: Long, authorId: Long, request: PostUpdateRequest): PostResponse {
        val post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("Post not found: $postId") }
        
        // Assignment를 통해 권한 확인
        val assignment = assignmentRepository.findById(post.assignmentId)
            .orElseThrow { IllegalArgumentException("Assignment not found: ${post.assignmentId}") }
        
        if (assignment.publisherMember.id != authorId) {
            throw IllegalArgumentException("Not authorized to edit this post")
        }
        
        val updatedPost = post.copy(
            content = request.content ?: post.content,
            status = request.status ?: post.status,
            updatedAt = LocalDateTime.now()
        )
        
        val savedPost = postRepository.save(updatedPost)
        return createPostResponse(savedPost)
    }

    @Transactional
    fun deletePost(postId: Long, authorId: Long) {
        val post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("Post not found: $postId") }
        
        // Assignment를 통해 권한 확인
        val assignment = assignmentRepository.findById(post.assignmentId)
            .orElseThrow { IllegalArgumentException("Assignment not found: ${post.assignmentId}") }
        
        if (assignment.publisherMember.id != authorId) {
            throw IllegalArgumentException("Not authorized to delete this post")
        }
        
        postRepository.delete(post)
    }

    @Transactional
    fun getPost(postId: Long, incrementView: Boolean = false): PostResponse {
        var post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("Post not found: $postId") }
        
        if (incrementView && post.isVisible()) {
            post = post.incrementViewCount()
            post = postRepository.save(post)
        }
        
        return createPostResponse(post)
    }

    fun getPublishedPosts(pageable: Pageable): Page<PostListResponse> {
        return postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.PUBLISHED, pageable)
            .map { PostListResponse.from(it) }
    }

    fun getPostsByAuthor(authorId: Long, pageable: Pageable): Page<PostListResponse> {
        val author = memberRepository.findById(authorId)
            .orElseThrow { IllegalArgumentException("Member not found: $authorId") }
        
        return postRepository.findByAuthorOrderByCreatedAtDesc(author, pageable)
            .map { PostListResponse.from(it) }
    }

    fun searchPosts(request: PostSearchRequest): Page<PostListResponse> {
        val pageable = PageRequest.of(request.page, request.size)
        
        return when {
            request.keyword != null && request.status != null -> {
                postRepository.findByStatusAndKeywordOrderByCreatedAtDesc(
                    request.status, request.keyword, pageable
                )
            }
            request.status != null -> {
                postRepository.findByStatusOrderByCreatedAtDesc(request.status, pageable)
            }
            else -> {
                postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.PUBLISHED, pageable)
            }
        }.map { PostListResponse.from(it) }
    }

    fun getFeaturedPosts(pageable: Pageable): Page<PostListResponse> {
        return postRepository.findFeaturedPosts(LocalDateTime.now(), pageable)
            .map { PostListResponse.from(it) }
    }

    fun getPopularPosts(pageable: Pageable): Page<PostListResponse> {
        return postRepository.findPopularPosts(pageable)
            .map { PostListResponse.from(it) }
    }

    @Transactional
    fun publishPost(postId: Long, authorId: Long): PostResponse {
        val post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("Post not found: $postId") }
        
        // Assignment를 통해 권한 확인
        val assignment = assignmentRepository.findById(post.assignmentId)
            .orElseThrow { IllegalArgumentException("Assignment not found: ${post.assignmentId}") }
        
        if (assignment.publisherMember.id != authorId) {
            throw IllegalArgumentException("Not authorized to publish this post")
        }
        
        val publishedPost = post.publish()
        val savedPost = postRepository.save(publishedPost)
        return createPostResponse(savedPost)
    }

    @Transactional
    fun rejectPost(postId: Long, reviewerId: Long, reason: String): PostResponse {
        val post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("Post not found: $postId") }
        
        val reviewer = memberRepository.findById(reviewerId)
            .orElseThrow { IllegalArgumentException("Reviewer not found: $reviewerId") }
        
        val rejectedPost = post.reject(reason).copy(reviewedBy = reviewer)
        val savedPost = postRepository.save(rejectedPost)
        return createPostResponse(savedPost)
    }

    @Transactional
    fun setFeatured(postId: Long, featured: Boolean, featuredUntil: LocalDateTime? = null): PostResponse {
        val post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("Post not found: $postId") }
        
        val featuredPost = post.setFeatured(featured, featuredUntil)
        val savedPost = postRepository.save(featuredPost)
        return createPostResponse(savedPost)
    }

    @Transactional
    fun approvePost(postId: Long, reviewerId: Long): PostResponse {
        val post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("Post not found: $postId") }
        
        val reviewer = memberRepository.findById(reviewerId)
            .orElseThrow { IllegalArgumentException("Reviewer not found: $reviewerId") }
        
        val approvedPost = post.approve().copy(reviewedBy = reviewer)
        val savedPost = postRepository.save(approvedPost)
        return createPostResponse(savedPost)
    }

    fun getPostStats(): PostStatsResponse {
        return PostStatsResponse(
            totalPosts = postRepository.count(),
            publishedPosts = postRepository.countByStatus(PostStatus.PUBLISHED),
            draftPosts = postRepository.countByStatus(PostStatus.PENDING),
            hiddenPosts = postRepository.countByStatus(PostStatus.REJECTED)
        )
    }

    @Transactional
    fun generateDraftPostsForRound(roundId: Long): List<PostResponse> {
        val round = roundRepository.findById(roundId)
            .orElseThrow { IllegalArgumentException("Round not found: $roundId") }
            
        // 결제 완료된 주문들의 AdTask만 조회
        val adTaskIds = adTaskRepository.findByRoundId(roundId).map { it.id!! }
        val allOrders = orderRepository.findByAdTaskIdIn(adTaskIds)
        
        val paidOrders = allOrders.filter { order ->
            // PAYMENT_CONFIRMED 이상 상태인 주문만 포함
            order.status in listOf(
                OrderStatus.PAYMENT_CONFIRMED,
                OrderStatus.APPROVED,
                OrderStatus.IN_PROGRESS,
                OrderStatus.COMPLETED
            )
        }
        
        if (paidOrders.isEmpty()) {
            throw IllegalStateException("라운드 $roundId 에 결제 완료된 주문이 없습니다.")
        }
        
        val adTasks = paidOrders.map { it.adTask }
        val participants = adTasks.map { it.member }.distinct()
        
        // 각 참여자는 정확히 하나의 AdTask를 가져야 함
        val memberToAdTaskCount = adTasks.groupBy { it.member }.mapValues { it.value.size }
        val duplicateMembers = memberToAdTaskCount.filter { it.value > 1 }
        
        if (duplicateMembers.isNotEmpty()) {
            val duplicateMemberNames = duplicateMembers.keys.map { it.companyName ?: "Unknown" }
            throw IllegalStateException("다음 참여자들이 여러 개의 결제 완료 AdTask를 가지고 있습니다: ${duplicateMemberNames.joinToString(", ")}")
        }
        
        // 순환발주 비용 계산
        val circularSolution = circularAssignmentService.calculateCircularAssignments(participants, adTasks)
        
        if (!circularSolution.isValid) {
            throw IllegalStateException("순환발주 계산 실패: ${circularSolution.errorMessage}")
        }
        
        val generatedPosts = mutableListOf<PostResponse>()
        
        // 각 assignment에 대해 advertisement_assignments 테이블에 레코드 생성 후 게시글 생성
        for (circularAssignment in circularSolution.assignments) {
            val publisherMember = circularAssignment.author
            val targetAd = circularAssignment.targetAdTask
            val assignedCost = circularAssignment.assignedCost
            val advertiserMember = targetAd.member
            
            // 이미 존재하는 assignment 체크
            val existingAssignment = assignmentRepository.findByRoundAndPublisherAndAd(roundId, publisherMember.id!!, targetAd.id!!)
            
            val assignment = if (existingAssignment != null) {
                existingAssignment
            } else {
                // 새로운 Assignment 생성
                val newAssignment = Assignment(
                    round = round,
                    advertiserMember = advertiserMember,
                    publisherMember = publisherMember,
                    adTask = targetAd,
                    revenuePerPost = assignedCost,
                    assignmentStatus = AssignmentStatus.ASSIGNED
                )
                assignmentRepository.save(newAssignment)
            }
            
            // 이미 존재하는 게시글 체크
            val existingPost = postRepository.findByAssignmentId(assignment.id!!)
            
            if (existingPost == null) {
                // AdTask ID로 관련된 Order 찾기
                val relatedOrder = orderRepository.findByAdTaskId(targetAd.id!!)
                val adTitle = relatedOrder?.productName ?: "광고 콘텐츠"
                val companyName = advertiserMember.companyName ?: "회사"
                
                val draftPost = Post(
                    assignmentId = assignment.id!!,
                    content = "# ${adTitle} 소개\n\n이 글은 ${companyName}의 '${adTitle}'에 대한 소개글입니다.\n\n[여기에 내용을 작성해주세요]",
                    status = PostStatus.PENDING,
                    finalRevenue = assignedCost
                )
                
                val savedPost = postRepository.save(draftPost)
                generatedPosts.add(createPostResponse(savedPost))
            }
        }
        return generatedPosts
    }

    private fun createPostResponse(post: Post): PostResponse {
        val assignment = assignmentRepository.findById(post.assignmentId)
            .orElseThrow { IllegalArgumentException("Assignment not found: ${post.assignmentId}") }
        
        return PostResponse(
            id = post.id!!,
            title = "게시글 #${post.id}",
            content = post.content ?: "",
            authorId = assignment.publisherMember.id!!,
            authorName = assignment.publisherMember.companyName ?: "Unknown",
            roundId = assignment.round.id!!,
            roundTitle = assignment.round.title ?: "Unknown Round",
            targetAdTaskId = assignment.adTask.id!!,
            targetAdContent = assignment.adTask.adContent ?: "",
            assignedCost = post.finalRevenue ?: assignment.revenuePerPost,
            postType = PostType.BLOG_INTRODUCTION,
            status = post.status,
            postStartDate = assignment.round.startDate ?: LocalDateTime.now(),
            postEndDate = assignment.round.endDate ?: LocalDateTime.now(),
            publishedAt = post.publishedAt,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt,
            isActive = true // 임시로 true
        )
    }
}