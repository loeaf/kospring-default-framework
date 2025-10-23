package com.service.frame.post.service

import com.service.frame.post.dto.*
import com.service.frame.post.entity.AdvertisementPostStatus
import com.service.frame.post.repository.AdvertisementAssignmentRepository
import com.service.frame.post.repository.AdvertisementPostRepository
import com.service.frame.member.repository.MemberRepository
import com.service.frame.order.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class AdvertisementPostService(
    private val postRepository: AdvertisementPostRepository,
    private val assignmentRepository: AdvertisementAssignmentRepository,
    private val memberRepository: MemberRepository,
    private val orderRepository: OrderRepository,
    private val orderService: com.service.frame.order.service.OrderService,
    private val revenueService: com.service.frame.revenue.service.RevenueService
) {
    
    // 내 할당된 포스트 목록 조회 (publisher 기준)
    fun getMyAssignedPosts(publisherMemberId: Long): AdvertisementPostListResponse {
        val posts = postRepository.findByPublisherMemberIdOrderBySubmittedAtDesc(publisherMemberId)
        
        val postResponses = posts.map { post ->
            MyAssignedPostResponse(
                assignmentId = post.assignment?.id ?: 0,
                postId = post.id,
                roundId = post.assignment?.round?.id ?: 0,
                roundTitle = post.assignment?.round?.title ?: "Round #${post.assignment?.round?.id}",
                roundStartDate = post.assignment?.round?.getCalculatedPostStartDate(),
                roundEndDate = post.assignment?.round?.getCalculatedPostEndDate(),
                advertiserMemberId = post.assignment?.advertiserMember?.id ?: 0,
                advertiserEmail = post.assignment?.advertiserMember?.email ?: "",
                advertiserCompanyName = post.assignment?.advertiserMember?.companyName ?: "",
                adTaskId = post.assignment?.adTask?.id ?: 0,
                revenuePerPost = post.assignment?.revenuePerPost ?: BigDecimal.ZERO,
                assignmentStatus = post.assignment?.assignmentStatus?.name ?: "",
                postContent = post.content,
                postStatus = post.postStatus.name,
                ctrRate = post.ctrRate,
                finalRevenue = post.finalRevenue,
                submittedAt = post.submittedAt,
                approvedAt = post.approvedAt,
                publishedAt = post.publishedAt,
                notes = post.notes,
                rejectionReason = post.rejectionReason,
                createdAt = post.createdAt
            )
        }
        
        return AdvertisementPostListResponse(
            posts = postResponses,
            totalCount = postResponses.size,
            pendingCount = postResponses.count { it.postStatus == "PENDING" },
            approvedCount = postResponses.count { it.postStatus == "APPROVED" },
            publishedCount = postResponses.count { it.postStatus == "PUBLISHED" },
            rejectedCount = postResponses.count { it.postStatus == "REJECTED" }
        )
    }
    
    // 특정 포스트 상세 조회
    fun getPostDetail(postId: Long): PostDetailResponse {
        val post = postRepository.findById(postId).orElse(null)
            ?: throw IllegalArgumentException("Post not found with id: $postId")
        
        return mapToPostDetailResponse(post)
    }
    
    // 포스트 내용 작성/수정
    @Transactional
    fun updatePostContent(postId: Long, request: PostContentUpdateRequest, userId: Long): PostDetailResponse {
        val post = postRepository.findById(postId).orElse(null)
            ?: throw IllegalArgumentException("Post not found with id: $postId")
        
        // 권한 확인: publisher만 수정 가능
        if (post.assignment?.publisherMember?.id != userId) {
            throw IllegalArgumentException("You are not the publisher of this post")
        }
        
        val updatedPost = post.copy(
            content = request.content,
            notes = request.notes,
            postStatus = AdvertisementPostStatus.PENDING, // 내용 수정 시 다시 PENDING 상태로
            updatedAt = LocalDateTime.now()
        )
        
        val savedPost = postRepository.save(updatedPost)
        
        // Assignment 상태 업데이트 (최초 작성 vs 수정 구분)
        savedPost.assignment?.let { assignment ->
            val newAssignmentStatus = if (post.content.isNullOrBlank()) {
                com.service.frame.post.entity.AssignmentStatus.WRITTEN
            } else {
                com.service.frame.post.entity.AssignmentStatus.WRITTEN
            }
            
            val updatedAssignment = assignment.copy(
                assignmentStatus = newAssignmentStatus,
                updatedAt = LocalDateTime.now()
            )
            assignmentRepository.save(updatedAssignment)
        }
        
        return mapToPostDetailResponse(savedPost)
    }
    
    // 포스트 상태 변경
    @Transactional
    fun updatePostStatus(postId: Long, request: PostStatusUpdateRequest, userId: Long): PostDetailResponse {
        val post = postRepository.findById(postId).orElse(null)
            ?: throw IllegalArgumentException("Post not found with id: $postId")
        
        val newStatus = AdvertisementPostStatus.valueOf(request.postStatus)
        
        // 권한 확인
        validateStatusChangePermission(post, newStatus, userId)
        
        val now = LocalDateTime.now()
        val updatedPost = post.copy(
            postStatus = newStatus,
            notes = request.notes ?: post.notes,
            rejectionReason = request.rejectionReason ?: post.rejectionReason,
            failureReason = request.failureReason ?: post.failureReason,
            ctrRate = request.ctrRate ?: post.ctrRate,
            finalRevenue = request.finalRevenue ?: post.finalRevenue,
            approvedAt = if (newStatus == AdvertisementPostStatus.APPROVED && post.approvedAt == null) now else post.approvedAt,
            publishedAt = if (newStatus == AdvertisementPostStatus.PUBLISHED && post.publishedAt == null) now else post.publishedAt,
            failedAt = if (newStatus == AdvertisementPostStatus.FAILED && post.failedAt == null) now else post.failedAt,
            reviewedBy = if (newStatus in listOf(AdvertisementPostStatus.APPROVED, AdvertisementPostStatus.REJECTED)) {
                memberRepository.findById(userId).orElse(null)
            } else post.reviewedBy,
            updatedAt = now
        )
        
        val savedPost = postRepository.save(updatedPost)
        
        // 포스트 상태에 따라 Assignment 상태도 업데이트
        savedPost.assignment?.let { assignment ->
            val newAssignmentStatus = when (newStatus) {
                AdvertisementPostStatus.APPROVED -> com.service.frame.post.entity.AssignmentStatus.COMPLETED
                AdvertisementPostStatus.PUBLISHED -> com.service.frame.post.entity.AssignmentStatus.COMPLETED
                AdvertisementPostStatus.FAILED -> com.service.frame.post.entity.AssignmentStatus.FAILED
                else -> assignment.assignmentStatus // 다른 상태는 그대로 유지
            }
            
            if (newAssignmentStatus != assignment.assignmentStatus) {
                val updatedAssignment = assignment.copy(
                    assignmentStatus = newAssignmentStatus,
                    updatedAt = now
                )
                assignmentRepository.save(updatedAssignment)
            }
        }
        
        // 포스트 상태가 PUBLISHED로 변경되었을 때 수익 거래 기록 생성 및 주문 진행률 업데이트
        if (newStatus == AdvertisementPostStatus.PUBLISHED) {
            try {
                // INCOME 거래 기록 생성
                revenueService.createIncomeTransaction(savedPost)
                
                // 주문 진행률 업데이트
                val roundId = savedPost.assignment?.round?.id
                if (roundId != null) {
                    orderService.updateOrderProgressByRound(roundId)
                }
            } catch (e: Exception) {
                // 거래 기록 생성이나 주문 진행률 업데이트 실패가 포스트 업데이트를 막지 않도록 함
                println("포스트 게시 후속 처리 중 오류 발생: ${e.message}")
            }
        }
        
        return mapToPostDetailResponse(savedPost)
    }
    
    // 할당된 포스트 중 특정 assignment의 포스트 조회
    fun getPostByAssignment(assignmentId: Long): PostDetailResponse {
        val post = postRepository.findByAssignmentId(assignmentId)
            ?: throw IllegalArgumentException("Post not found for assignment: $assignmentId")
        
        return mapToPostDetailResponse(post)
    }
    
    // 라운드별 내 할당 현황 조회
    fun getMyAssignmentsByRound(roundId: Long, publisherMemberId: Long): AdvertisementPostListResponse {
        val posts = postRepository.findByPublisherMemberIdAndRoundIdOrderBySubmittedAtDesc(publisherMemberId, roundId)
        
        val postResponses = posts.map { post ->
            MyAssignedPostResponse(
                assignmentId = post.assignment?.id ?: 0,
                postId = post.id,
                roundId = post.assignment?.round?.id ?: 0,
                roundTitle = post.assignment?.round?.title ?: "Round #${post.assignment?.round?.id}",
                roundStartDate = post.assignment?.round?.getCalculatedPostStartDate(),
                roundEndDate = post.assignment?.round?.getCalculatedPostEndDate(),
                advertiserMemberId = post.assignment?.advertiserMember?.id ?: 0,
                advertiserEmail = post.assignment?.advertiserMember?.email ?: "",
                advertiserCompanyName = post.assignment?.advertiserMember?.companyName ?: "",
                adTaskId = post.assignment?.adTask?.id ?: 0,
                revenuePerPost = post.assignment?.revenuePerPost ?: BigDecimal.ZERO,
                assignmentStatus = post.assignment?.assignmentStatus?.name ?: "",
                postContent = post.content,
                postStatus = post.postStatus.name,
                ctrRate = post.ctrRate,
                finalRevenue = post.finalRevenue,
                submittedAt = post.submittedAt,
                approvedAt = post.approvedAt,
                publishedAt = post.publishedAt,
                notes = post.notes,
                rejectionReason = post.rejectionReason,
                createdAt = post.createdAt
            )
        }
        
        return AdvertisementPostListResponse(
            posts = postResponses,
            totalCount = postResponses.size,
            pendingCount = postResponses.count { it.postStatus == "PENDING" },
            approvedCount = postResponses.count { it.postStatus == "APPROVED" },
            publishedCount = postResponses.count { it.postStatus == "PUBLISHED" },
            rejectedCount = postResponses.count { it.postStatus == "REJECTED" }
        )
    }
    
    private fun validateStatusChangePermission(post: com.service.frame.post.entity.AdvertisementPost, newStatus: AdvertisementPostStatus, userId: Long) {
        when (newStatus) {
            AdvertisementPostStatus.APPROVED, AdvertisementPostStatus.REJECTED -> {
                // 관리자만 승인/거부 가능 (실제로는 관리자 권한 체크 로직 필요)
                // 현재는 publisher가 아닌 경우만 체크
                if (post.assignment?.publisherMember?.id == userId) {
                    throw IllegalArgumentException("Publishers cannot approve or reject their own posts")
                }
            }
            AdvertisementPostStatus.PUBLISHED -> {
                // Publisher 또는 관리자가 게시 가능
                if (post.assignment?.publisherMember?.id != userId) {
                    // 관리자 권한 체크 (현재는 단순히 허용)
                }
            }
            AdvertisementPostStatus.FAILED -> {
                // 관리자만 실패 처리 가능
                if (post.assignment?.publisherMember?.id == userId) {
                    throw IllegalArgumentException("Publishers cannot mark their own posts as failed")
                }
            }
            AdvertisementPostStatus.PENDING -> {
                // 누구나 PENDING으로 변경 가능 (재제출)
            }
        }
    }
    
    private fun mapToPostDetailResponse(post: com.service.frame.post.entity.AdvertisementPost): PostDetailResponse {
        // 게시 완료 시 광고 정보 포함
        val adTaskInfo = if (post.postStatus == AdvertisementPostStatus.PUBLISHED && 
                             post.assignment?.adTask != null && 
                             post.assignment?.adTask?.id != null) {
            val adTask = post.assignment!!.adTask!!
            // AdTask ID로 Order 조회하여 제품명과 요구사항 가져오기
            val order = orderRepository.findByAdTaskId(adTask.id!!)
            AdTaskInfo(
                id = adTask.id!!,
                webUrl = adTask.webUrl,
                adType = adTask.adType ?: "UNKNOWN",
                adIndex = adTask.adIndex ?: 0,
                productName = order?.productName,
                requirements = order?.requirements,
                status = adTask.status.name
            )
        } else null
        
        return PostDetailResponse(
            id = post.id!!,
            assignmentId = post.assignment?.id ?: 0,
            content = post.content,
            ctrRate = post.ctrRate,
            finalRevenue = post.finalRevenue,
            postStatus = post.postStatus.name,
            submittedAt = post.submittedAt,
            approvedAt = post.approvedAt,
            publishedAt = post.publishedAt,
            failedAt = post.failedAt,
            failureReason = post.failureReason,
            rejectionReason = post.rejectionReason,
            reviewedBy = post.reviewedBy?.id,
            reviewedByName = post.reviewedBy?.companyName,
            notes = post.notes,
            assignment = AssignmentInfo(
                id = post.assignment?.id ?: 0,
                roundId = post.assignment?.round?.id ?: 0,
                roundTitle = post.assignment?.round?.title ?: "Round #${post.assignment?.round?.id}",
                roundStartDate = post.assignment?.round?.getCalculatedPostStartDate(),
                roundEndDate = post.assignment?.round?.getCalculatedPostEndDate(),
                advertiserMemberId = post.assignment?.advertiserMember?.id ?: 0,
                advertiserEmail = post.assignment?.advertiserMember?.email ?: "",
                advertiserCompanyName = post.assignment?.advertiserMember?.companyName ?: "",
                publisherMemberId = post.assignment?.publisherMember?.id ?: 0,
                publisherEmail = post.assignment?.publisherMember?.email ?: "",
                publisherCompanyName = post.assignment?.publisherMember?.companyName ?: "",
                adTaskId = post.assignment?.adTask?.id ?: 0,
                revenuePerPost = post.assignment?.revenuePerPost ?: BigDecimal.ZERO,
                assignmentStatus = post.assignment?.assignmentStatus?.name ?: ""
            ),
            adTaskInfo = adTaskInfo,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt
        )
    }
    
    // 테스트용: 라운드의 모든 할당에 대해 포스트 일괄 생성
    @Transactional
    fun createTestPostsForRound(roundId: Long): List<PostDetailResponse> {
        val assignments = assignmentRepository.findByRoundIdOrderByCreatedAtDesc(roundId)
        
        if (assignments.isEmpty()) {
            throw IllegalArgumentException("No assignments found for round: $roundId")
        }
        
        val posts = assignments.mapNotNull { assignment ->
            // 이미 포스트가 있는지 확인
            val existingPost = postRepository.findByAssignmentId(assignment.id!!)
            if (existingPost != null) {
                // 기존 포스트 업데이트
                val updatedPost = existingPost.copy(
                    content = "테스트 광고 포스트 내용입니다. 광고주: ${assignment.advertiserMember?.companyName ?: assignment.advertiserMember?.email ?: ""}, " +
                             "게시자: ${assignment.publisherMember?.companyName ?: assignment.publisherMember?.email ?: ""}. " +
                             "이 포스트는 자동으로 업데이트된 테스트 내용입니다.",
                    postStatus = AdvertisementPostStatus.PENDING,
                    notes = "테스트용 자동 업데이트된 포스트",
                    updatedAt = LocalDateTime.now()
                )
                return@mapNotNull postRepository.save(updatedPost)
            }
            
            // 새 포스트 생성
            val post = com.service.frame.post.entity.AdvertisementPost(
                assignment = assignment,
                content = "테스트 광고 포스트 내용입니다. 광고주: ${assignment.advertiserMember?.companyName ?: assignment.advertiserMember?.email ?: ""}, " +
                         "게시자: ${assignment.publisherMember?.companyName ?: assignment.publisherMember?.email ?: ""}. " +
                         "이 포스트는 자동으로 생성된 테스트 내용입니다.",
                postStatus = AdvertisementPostStatus.PENDING,
                notes = "테스트용 자동 생성된 포스트"
            )
            
            postRepository.save(post)
        }
        
        return posts.map { mapToPostDetailResponse(it) }
    }
    
    // 테스트용: 라운드의 모든 포스트를 특정 상태로 일괄 변경
    @Transactional
    fun updateAllPostStatusInRound(roundId: Long, targetStatus: String, adminUserId: Long): List<PostDetailResponse> {
        // 라운드의 모든 할당에서 포스트 조회
        val posts = assignmentRepository.findByRoundIdOrderByCreatedAtDesc(roundId)
            .mapNotNull { assignment ->
                postRepository.findByAssignmentId(assignment.id!!)
            }
        
        if (posts.isEmpty()) {
            return emptyList()
        }
        
        val newStatus = AdvertisementPostStatus.valueOf(targetStatus)
        val now = LocalDateTime.now()
        
        val updatedPosts = posts.map { post ->
            val updatedPost = post.copy(
                postStatus = newStatus,
                notes = "테스트용 일괄 상태 변경",
                approvedAt = if (newStatus == AdvertisementPostStatus.APPROVED && post.approvedAt == null) now else post.approvedAt,
                publishedAt = if (newStatus == AdvertisementPostStatus.PUBLISHED && post.publishedAt == null) now else post.publishedAt,
                failedAt = if (newStatus == AdvertisementPostStatus.FAILED && post.failedAt == null) now else post.failedAt,
                reviewedBy = if (newStatus in listOf(AdvertisementPostStatus.APPROVED, AdvertisementPostStatus.REJECTED)) {
                    memberRepository.findById(adminUserId).orElse(null)
                } else post.reviewedBy,
                updatedAt = now
            )
            
            val savedPost = postRepository.save(updatedPost)
            
            // 포스트 상태에 따라 Assignment 상태도 업데이트
            savedPost.assignment?.let { assignment ->
                val newAssignmentStatus = when (newStatus) {
                    AdvertisementPostStatus.APPROVED -> com.service.frame.post.entity.AssignmentStatus.COMPLETED
                    AdvertisementPostStatus.PUBLISHED -> com.service.frame.post.entity.AssignmentStatus.COMPLETED
                    AdvertisementPostStatus.FAILED -> com.service.frame.post.entity.AssignmentStatus.FAILED
                    else -> assignment.assignmentStatus // 다른 상태는 그대로 유지
                }
                
                if (newAssignmentStatus != assignment.assignmentStatus) {
                    val updatedAssignment = assignment.copy(
                        assignmentStatus = newAssignmentStatus,
                        updatedAt = now
                    )
                    assignmentRepository.save(updatedAssignment)
                }
            }
            
            savedPost
        }
        
        // 상태 변경 후 수익 거래 기록 생성 및 주문 진행률 업데이트
        if (newStatus == AdvertisementPostStatus.PUBLISHED) {
            try {
                // 일괄 게시된 포스트들에 대해 INCOME 거래 기록 생성
                updatedPosts.forEach { post ->
                    revenueService.createIncomeTransaction(post)
                }
                
                // 주문 진행률 업데이트
                orderService.updateOrderProgressByRound(roundId)
            } catch (e: Exception) {
                println("일괄 포스트 게시 후속 처리 중 오류 발생: ${e.message}")
            }
        }
        
        return updatedPosts.map { mapToPostDetailResponse(it) }
    }
    
    // 테스트용: 라운드의 포스트 전체 워크플로우 실행 (생성 → 승인 → 게시)
    @Transactional
    fun executeTestWorkflowForRound(roundId: Long, adminUserId: Long): Map<String, List<PostDetailResponse>> {
        // 1. 포스트 생성
        val createdPosts = createTestPostsForRound(roundId)
        
        // 2. 모든 포스트 승인
        Thread.sleep(1000) // 약간의 시간 간격
        val approvedPosts = updateAllPostStatusInRound(roundId, "APPROVED", adminUserId)
        
        // 3. 모든 포스트 게시
        Thread.sleep(1000) // 약간의 시간 간격
        val publishedPosts = updateAllPostStatusInRound(roundId, "PUBLISHED", adminUserId)
        
        return mapOf(
            "created" to createdPosts,
            "approved" to approvedPosts,
            "published" to publishedPosts
        )
    }
    
    // 테스트용: 라운드의 포스트 목록 조회 (모든 publisher)
    fun getAllPostsInRound(roundId: Long): List<PostDetailResponse> {
        val assignments = assignmentRepository.findByRoundIdOrderByCreatedAtDesc(roundId)
        
        val posts = assignments.mapNotNull { assignment ->
            postRepository.findByAssignmentId(assignment.id!!)
        }
        
        return posts.map { mapToPostDetailResponse(it) }
    }
}