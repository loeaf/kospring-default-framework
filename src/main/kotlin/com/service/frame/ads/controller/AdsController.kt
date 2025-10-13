package com.service.frame.ads.controller

import com.service.frame.ads.dto.*
import com.service.frame.ads.service.AdsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ads")
class AdsController(
    private val adsService: AdsService
) {
    
    /**
     * 사용자의 광고 목록 조회
     */
    @GetMapping("/my-ads")
    fun getUserAds(
        @RequestParam("memberId") memberId: Long,
        @RequestParam("status", required = false) status: AdStatus?,
        @RequestParam("limit", defaultValue = "20") limit: Int
    ): ResponseEntity<UserAdsResponse> {
        val result = adsService.getUserAds(memberId, status, limit)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 최근 광고 목록 조회 (마이페이지용)
     */
    @GetMapping("/recent")
    fun getRecentUserAds(
        @RequestParam("memberId") memberId: Long,
        @RequestParam("limit", defaultValue = "5") limit: Int
    ): ResponseEntity<List<UserAdItem>> {
        val result = adsService.getRecentUserAds(memberId, limit)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 광고 상세 조회
     */
    @GetMapping("/{adId}")
    fun getAdDetail(
        @PathVariable adId: Long,
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<AdDetailResponse> {
        val result = adsService.getAdDetail(adId, memberId)
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * 공개 광고 갤러리 조회
     */
    @GetMapping("/gallery")
    fun getPublicAdGallery(
        @RequestParam("category", required = false) category: String?,
        @RequestParam("limit", defaultValue = "50") limit: Int
    ): ResponseEntity<AdGalleryResponse> {
        val result = adsService.getPublicAdGallery(category, limit)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 새 광고 생성
     */
    @PostMapping("")
    fun createAd(
        @RequestParam("memberId") memberId: Long,
        @RequestBody request: AdCreateRequest
    ): ResponseEntity<UserAdItem> {
        val result = adsService.createAd(memberId, request)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 광고 정보 수정
     */
    @PutMapping("/{adId}")
    fun updateAd(
        @PathVariable adId: Long,
        @RequestParam("memberId") memberId: Long,
        @RequestBody request: AdUpdateRequest
    ): ResponseEntity<UserAdItem> {
        val result = adsService.updateAd(adId, memberId, request)
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * 광고 삭제
     */
    @DeleteMapping("/{adId}")
    fun deleteAd(
        @PathVariable adId: Long,
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<Map<String, Any>> {
        val success = adsService.deleteAd(adId, memberId)
        return if (success) {
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "광고가 삭제되었습니다."
            ))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * 광고 생성 재시도
     */
    @PostMapping("/{adId}/retry")
    fun retryAdGeneration(
        @PathVariable adId: Long,
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<UserAdItem> {
        val result = adsService.retryAdGeneration(adId, memberId)
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * 광고 상태별 통계
     */
    @GetMapping("/stats")
    fun getAdStats(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<AdStatusCounts> {
        val result = adsService.getUserAds(memberId)
        return ResponseEntity.ok(result.statusCounts)
    }
    
    /**
     * 광고 HTML 파일 직접 접근
     */
    @GetMapping("/{adId}/preview")
    fun getAdPreview(
        @PathVariable adId: Long,
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<Map<String, String>> {
        val ad = adsService.getAdDetail(adId, memberId)
        return if (ad?.htmlFilePath != null) {
            ResponseEntity.ok(mapOf(
                "htmlFilePath" to ad.htmlFilePath!!,
                "previewUrl" to (ad.previewUrl ?: "/ads/${ad.htmlFilePath}")
            ))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * 광고 카테고리 목록 조회
     */
    @GetMapping("/categories")
    fun getAdCategories(): ResponseEntity<List<String>> {
        val categories = listOf(
            "헬스케어", "금융", "교육", "기술", "뷰티", "부동산",
            "자동차", "여행", "음식", "패션", "스포츠", "엔터테인먼트"
        )
        return ResponseEntity.ok(categories)
    }
    
    /**
     * 테스트용 API - 광고 데이터 생성
     */
    @PostMapping("/test/generate")
    fun generateTestAds(
        @RequestParam("memberId") memberId: Long,
        @RequestParam("count", defaultValue = "5") count: Int
    ): ResponseEntity<Map<String, Any>> {
        // 테스트용 광고 데이터 생성
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "테스트 광고 ${count}개가 생성되었습니다.",
            "generatedCount" to count,
            "memberId" to memberId
        ))
    }
}