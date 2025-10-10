package com.service.frame.post.controller

import com.service.frame.post.dto.*
import com.service.frame.post.service.AdvertisementPostService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/advertisement-posts")
class AdvertisementPostController(
    private val advertisementPostService: AdvertisementPostService
) {

    /**
     * 멤버의 할당된 포스트 목록 조회 (publisher 기준)
     */
    @GetMapping("/members/{memberId}/posts")
    fun getMemberPosts(
        @PathVariable memberId: Long
    ): ResponseEntity<AdvertisementPostListResponse> {
        val result = advertisementPostService.getMyAssignedPosts(memberId)
        return ResponseEntity.ok(result)
    }

    /**
     * 특정 라운드에서 멤버의 할당된 포스트 목록 조회
     */
    @GetMapping("/rounds/{roundId}/members/{memberId}/posts")
    fun getMemberPostsByRound(
        @PathVariable roundId: Long,
        @PathVariable memberId: Long
    ): ResponseEntity<AdvertisementPostListResponse> {
        val result = advertisementPostService.getMyAssignmentsByRound(roundId, memberId)
        return ResponseEntity.ok(result)
    }

    /**
     * 포스트 상세 조회
     */
    @GetMapping("/posts/{postId}")
    fun getPostDetail(@PathVariable postId: Long): ResponseEntity<PostDetailResponse> {
        val result = advertisementPostService.getPostDetail(postId)
        return ResponseEntity.ok(result)
    }

    /**
     * 할당 ID로 포스트 조회
     */
    @GetMapping("/assignments/{assignmentId}/post")
    fun getPostByAssignment(@PathVariable assignmentId: Long): ResponseEntity<PostDetailResponse> {
        val result = advertisementPostService.getPostByAssignment(assignmentId)
        return ResponseEntity.ok(result)
    }

    /**
     * 포스트 내용 작성/수정
     */
    @PutMapping("/posts/{postId}/content")
    fun updatePostContent(
        @PathVariable postId: Long,
        @RequestBody request: PostContentUpdateRequest,
        @RequestParam("userId") userId: Long
    ): ResponseEntity<PostDetailResponse> {
        val result = advertisementPostService.updatePostContent(postId, request, userId)
        return ResponseEntity.ok(result)
    }

    /**
     * 포스트 상태 변경
     */
    @PutMapping("/posts/{postId}/status")
    fun updatePostStatus(
        @PathVariable postId: Long,
        @RequestBody request: PostStatusUpdateRequest,
        @RequestParam("userId") userId: Long
    ): ResponseEntity<PostDetailResponse> {
        val result = advertisementPostService.updatePostStatus(postId, request, userId)
        return ResponseEntity.ok(result)
    }

    /**
     * 테스트용 API - 라운드의 모든 할당에 대해 포스트 일괄 생성
     */
    @PostMapping("/test/rounds/{roundId}/create-all")
    fun createTestPostsForRound(@PathVariable roundId: Long): ResponseEntity<List<PostDetailResponse>> {
        val result = advertisementPostService.createTestPostsForRound(roundId)
        return ResponseEntity.ok(result)
    }

    /**
     * 테스트용 API - 라운드의 모든 포스트 상태 일괄 변경
     */
    @PutMapping("/test/rounds/{roundId}/status/{targetStatus}")
    fun updateAllPostStatusInRound(
        @PathVariable roundId: Long,
        @PathVariable targetStatus: String,
        @RequestParam("adminUserId", defaultValue = "1") adminUserId: Long
    ): ResponseEntity<List<PostDetailResponse>> {
        val result = advertisementPostService.updateAllPostStatusInRound(roundId, targetStatus, adminUserId)
        return ResponseEntity.ok(result)
    }

    /**
     * 테스트용 API - 라운드의 포스트 전체 워크플로우 실행 (생성 → 승인 → 게시)
     */
    @PostMapping("/test/rounds/{roundId}/full-workflow")
    fun executeTestWorkflowForRound(
        @PathVariable roundId: Long,
        @RequestParam("adminUserId", defaultValue = "1") adminUserId: Long
    ): ResponseEntity<Map<String, List<PostDetailResponse>>> {
        val result = advertisementPostService.executeTestWorkflowForRound(roundId, adminUserId)
        return ResponseEntity.ok(result)
    }

    /**
     * 테스트용 API - 라운드의 모든 포스트 조회
     */
    @GetMapping("/test/rounds/{roundId}/all-posts")
    fun getAllPostsInRound(@PathVariable roundId: Long): ResponseEntity<List<PostDetailResponse>> {
        val result = advertisementPostService.getAllPostsInRound(roundId)
        return ResponseEntity.ok(result)
    }
}