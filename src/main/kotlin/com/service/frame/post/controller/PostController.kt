package com.service.frame.post.controller

import com.service.frame.post.dto.*
import com.service.frame.post.entity.PostStatus
import com.service.frame.post.service.PostService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.validation.Valid

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = ["*"])
class PostController(
    private val postService: PostService
) {

    @PostMapping("/members/{memberId}/posts")
    fun createPost(
        @PathVariable memberId: Long,
        @Valid @RequestBody request: PostCreateRequest
    ): ResponseEntity<PostResponse> {
        val post = postService.createPost(memberId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(post)
    }

    @GetMapping("/{id}")
    fun getPost(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "true") incrementView: Boolean
    ): ResponseEntity<PostResponse> {
        val post = postService.getPost(id, incrementView)
        return ResponseEntity.ok(post)
    }

    @PutMapping("/members/{memberId}/posts/{postId}")
    fun updatePost(
        @PathVariable memberId: Long,
        @PathVariable postId: Long,
        @Valid @RequestBody request: PostUpdateRequest
    ): ResponseEntity<PostResponse> {
        val post = postService.updatePost(postId, memberId, request)
        return ResponseEntity.ok(post)
    }

    @DeleteMapping("/members/{memberId}/posts/{postId}")
    fun deletePost(
        @PathVariable memberId: Long,
        @PathVariable postId: Long
    ): ResponseEntity<Void> {
        postService.deletePost(postId, memberId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostListResponse>> {
        val pageable = PageRequest.of(page, size)
        val posts = postService.getPublishedPosts(pageable)
        return ResponseEntity.ok(posts)
    }

    @GetMapping("/search")
    fun searchPosts(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) authorId: Long?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostListResponse>> {
        val request = PostSearchRequest(
            keyword = keyword,
            status = status?.let { enumValueOf<PostStatus>(it) },
            authorId = authorId,
            page = page,
            size = size
        )
        val posts = postService.searchPosts(request)
        return ResponseEntity.ok(posts)
    }

    @GetMapping("/author/{authorId}")
    fun getPostsByAuthor(
        @PathVariable authorId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PostListResponse>> {
        val pageable = PageRequest.of(page, size)
        val posts = postService.getPostsByAuthor(authorId, pageable)
        return ResponseEntity.ok(posts)
    }

    @GetMapping("/featured")
    fun getFeaturedPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<PostListResponse>> {
        val pageable = PageRequest.of(page, size)
        val posts = postService.getFeaturedPosts(pageable)
        return ResponseEntity.ok(posts)
    }

    @GetMapping("/popular")
    fun getPopularPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<PostListResponse>> {
        val pageable = PageRequest.of(page, size)
        val posts = postService.getPopularPosts(pageable)
        return ResponseEntity.ok(posts)
    }

    @PostMapping("/members/{memberId}/posts/{postId}/publish")
    fun publishPost(
        @PathVariable memberId: Long,
        @PathVariable postId: Long
    ): ResponseEntity<PostResponse> {
        val post = postService.publishPost(postId, memberId)
        return ResponseEntity.ok(post)
    }

    @PostMapping("/members/{reviewerId}/posts/{postId}/reject")
    fun rejectPost(
        @PathVariable reviewerId: Long,
        @PathVariable postId: Long,
        @RequestParam reason: String
    ): ResponseEntity<PostResponse> {
        val post = postService.rejectPost(postId, reviewerId, reason)
        return ResponseEntity.ok(post)
    }

    @PostMapping("/members/{reviewerId}/posts/{postId}/approve")
    fun approvePost(
        @PathVariable reviewerId: Long,
        @PathVariable postId: Long
    ): ResponseEntity<PostResponse> {
        val post = postService.approvePost(postId, reviewerId)
        return ResponseEntity.ok(post)
    }

    @PostMapping("/{id}/featured")
    fun setFeatured(
        @PathVariable id: Long,
        @RequestParam featured: Boolean,
        @RequestParam(required = false) featuredUntil: String?
    ): ResponseEntity<PostResponse> {
        val featuredUntilDateTime = featuredUntil?.let { LocalDateTime.parse(it) }
        val post = postService.setFeatured(id, featured, featuredUntilDateTime)
        return ResponseEntity.ok(post)
    }

    @GetMapping("/stats")
    fun getPostStats(): ResponseEntity<PostStatsResponse> {
        val stats = postService.getPostStats()
        return ResponseEntity.ok(stats)
    }

    // 순환발주 관련 API 엔드포인트들
    
    @PostMapping("/rounds/{roundId}/circular-assignments")
    fun createCircularAssignments(
        @PathVariable roundId: Long
    ): ResponseEntity<List<PostResponse>> {
        val posts = postService.generateDraftPostsForRound(roundId)
        return ResponseEntity.status(HttpStatus.CREATED).body(posts)
    }

    @GetMapping("/rounds/{roundId}/circular-assignments")
    fun getCircularAssignments(
        @PathVariable roundId: Long
    ): ResponseEntity<Map<String, Any>> {
        // TODO: 순환발주 할당 현황 조회 로직 구현
        return ResponseEntity.ok(mapOf(
            "roundId" to roundId,
            "message" to "순환발주 할당 현황 조회 기능은 추후 구현 예정"
        ))
    }

    @GetMapping("/rounds/{roundId}/balance-check")
    fun checkRoundBalance(
        @PathVariable roundId: Long
    ): ResponseEntity<Map<String, Any>> {
        // TODO: 라운드 밸런스 체크 로직 구현
        return ResponseEntity.ok(mapOf(
            "roundId" to roundId,
            "message" to "라운드 밸런스 체크 기능은 추후 구현 예정"
        ))
    }

    @GetMapping("/rounds/{roundId}/completion-status")
    fun getRoundCompletionStatus(
        @PathVariable roundId: Long
    ): ResponseEntity<Map<String, Any>> {
        // TODO: 라운드 완성도 체크 로직 구현
        return ResponseEntity.ok(mapOf(
            "roundId" to roundId,
            "message" to "라운드 완성도 체크 기능은 추후 구현 예정"
        ))
    }

    @GetMapping("/ads/{adTaskId}/post-stats")
    fun getAdTaskPostStats(
        @PathVariable adTaskId: Long
    ): ResponseEntity<Map<String, Any>> {
        // TODO: 광고별 게시글 통계 로직 구현
        return ResponseEntity.ok(mapOf(
            "adTaskId" to adTaskId,
            "message" to "광고별 게시글 통계 기능은 추후 구현 예정"
        ))
    }

    @GetMapping("/rounds/{roundId}/post-stats")
    fun getRoundPostStats(
        @PathVariable roundId: Long
    ): ResponseEntity<Map<String, Any>> {
        // TODO: 라운드별 게시글 통계 로직 구현
        return ResponseEntity.ok(mapOf(
            "roundId" to roundId,
            "message" to "라운드별 게시글 통계 기능은 추후 구현 예정"
        ))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid request")))
    }
}