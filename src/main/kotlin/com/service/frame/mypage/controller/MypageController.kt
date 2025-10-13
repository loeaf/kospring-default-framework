package com.service.frame.mypage.controller

import com.service.frame.mypage.dto.*
import com.service.frame.mypage.service.MypageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/mypage")
class MypageController(
    private val mypageService: MypageService
) {
    
    /**
     * 사용자 프로필 조회
     */
    @GetMapping("/profile")
    fun getUserProfile(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<UserProfileResponse> {
        val result = mypageService.getUserProfile(memberId)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 월간 통계 조회
     */
    @GetMapping("/stats/monthly")
    fun getMonthlyStats(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<MonthlyStatsResponse> {
        val result = mypageService.getMonthlyStats(memberId)
        return ResponseEntity.ok(result)
    }
    
    /**
     * AI 설정 조회
     */
    @GetMapping("/ai-settings")
    fun getAiSettings(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<AiSettingsResponse> {
        val result = mypageService.getAiSettings(memberId)
        return ResponseEntity.ok(result)
    }
    
    /**
     * AI 설정 업데이트
     */
    @PutMapping("/ai-settings")
    fun updateAiSettings(
        @RequestParam("memberId") memberId: Long,
        @RequestBody request: AiSettingsUpdateRequest
    ): ResponseEntity<AiSettingsResponse> {
        val result = mypageService.updateAiSettings(memberId, request)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 계정 정보 조회
     */
    @GetMapping("/account-info")
    fun getAccountInfo(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<AccountInfoResponse> {
        val result = mypageService.getAccountInfo(memberId)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 최근 활동 조회
     */
    @GetMapping("/activities")
    fun getRecentActivities(
        @RequestParam("memberId") memberId: Long,
        @RequestParam("limit", defaultValue = "10") limit: Int
    ): ResponseEntity<RecentActivityResponse> {
        val result = mypageService.getRecentActivities(memberId, limit)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 대시보드 요약 정보 조회 (모든 정보를 한 번에)
     */
    @GetMapping("/dashboard")
    fun getDashboardSummary(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<DashboardSummaryResponse> {
        val result = mypageService.getDashboardSummary(memberId)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 최근 광고 목록 조회 (마이페이지용)
     */
    @GetMapping("/recent-ads")
    fun getRecentAds(
        @RequestParam("memberId") memberId: Long,
        @RequestParam("limit", defaultValue = "5") limit: Int
    ): ResponseEntity<List<com.service.frame.ads.dto.UserAdItem>> {
        val result = mypageService.getRecentAds(memberId, limit)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 알림 설정 조회
     */
    @GetMapping("/notification-settings")
    fun getNotificationSettings(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<NotificationSettingsResponse> {
        val result = mypageService.getNotificationSettings(memberId)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 알림 설정 업데이트
     */
    @PutMapping("/notification-settings")
    fun updateNotificationSettings(
        @RequestParam("memberId") memberId: Long,
        @RequestBody request: NotificationSettingsUpdateRequest
    ): ResponseEntity<NotificationSettingsResponse> {
        val result = mypageService.updateNotificationSettings(memberId, request)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/account")
    fun deleteAccount(
        @RequestParam("memberId") memberId: Long,
        @RequestParam("reason", required = false) reason: String?
    ): ResponseEntity<Map<String, Any>> {
        // 실제로는 회원 탈퇴 로직 구현
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "회원 탈퇴가 완료되었습니다.",
            "deletedAt" to java.time.LocalDateTime.now()
        ))
    }
    
    /**
     * 비밀번호 변경
     */
    @PutMapping("/password")
    fun changePassword(
        @RequestParam("memberId") memberId: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Map<String, Any>> {
        val currentPassword = request["currentPassword"]
        val newPassword = request["newPassword"]
        
        if (currentPassword.isNullOrBlank() || newPassword.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to "현재 비밀번호와 새 비밀번호를 모두 입력해주세요."
            ))
        }
        
        // 실제로는 비밀번호 변경 로직 구현
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "비밀번호가 성공적으로 변경되었습니다."
        ))
    }
    
    /**
     * 프로필 이미지 업로드
     */
    @PostMapping("/profile/image")
    fun uploadProfileImage(
        @RequestParam("memberId") memberId: Long,
        @RequestParam("imageUrl") imageUrl: String
    ): ResponseEntity<Map<String, Any>> {
        // 실제로는 이미지 업로드 및 프로필 업데이트 로직
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "imageUrl" to imageUrl,
            "message" to "프로필 이미지가 업데이트되었습니다."
        ))
    }
    
    /**
     * 테스트용 API - 마이페이지 데이터 초기화
     */
    @PostMapping("/test/initialize")
    fun initializeTestData(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<Map<String, Any>> {
        // 테스트용 마이페이지 데이터 생성
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "테스트 마이페이지 데이터가 초기화되었습니다.",
            "memberId" to memberId
        ))
    }
    
    /**
     * 통계 새로고침 (캐시 갱신)
     */
    @PostMapping("/stats/refresh")
    fun refreshStats(
        @RequestParam("memberId") memberId: Long
    ): ResponseEntity<Map<String, Any>> {
        // 실제로는 통계 캐시 새로고침 로직
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "통계가 새로고침되었습니다.",
            "refreshedAt" to java.time.LocalDateTime.now()
        ))
    }
}