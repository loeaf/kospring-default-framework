package com.service.frame.ads.service

import com.service.frame.ads.dto.*
import com.service.frame.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class AdsService(
    private val memberRepository: MemberRepository
) {
    
    fun getUserAds(memberId: Long, status: AdStatus? = null, limit: Int = 20): UserAdsResponse {
        // 실제로는 광고 테이블에서 조회
        val ads = getUserAdsFromDb(memberId, status, limit)
        val statusCounts = calculateStatusCounts(memberId)
        
        return UserAdsResponse(
            ads = ads,
            totalCount = ads.size,
            statusCounts = statusCounts
        )
    }
    
    fun getRecentUserAds(memberId: Long, limit: Int = 5): List<UserAdItem> {
        // 최근 광고 목록 조회 (마이페이지용)
        return getUserAdsFromDb(memberId, null, limit)
    }
    
    fun getAdDetail(adId: Long, memberId: Long): AdDetailResponse? {
        // 광고 상세 정보 조회
        return getAdDetailFromDb(adId, memberId)
    }
    
    fun getPublicAdGallery(category: String? = null, limit: Int = 50): AdGalleryResponse {
        // 공개 광고 갤러리 조회
        val ads = getPublicAdsFromDb(category, limit)
        val categories = getAllCategories()
        
        return AdGalleryResponse(
            ads = ads,
            totalCount = ads.size,
            categories = categories
        )
    }
    
    @Transactional
    fun createAd(memberId: Long, request: AdCreateRequest): UserAdItem {
        // 새 광고 생성 요청
        val member = memberRepository.findById(memberId).orElse(null)
            ?: throw IllegalArgumentException("회원을 찾을 수 없습니다: $memberId")
        
        // 실제로는 광고 생성 서비스 호출
        return UserAdItem(
            id = System.currentTimeMillis(), // 임시 ID
            roundId = request.roundId,
            roundTitle = "Round #${request.roundId}",
            status = AdStatus.PENDING,
            adContent = null,
            htmlFilePath = null,
            previewUrl = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            completedAt = null,
            errorMessage = null,
            retryCount = 0,
            metadata = AdMetadata(
                tags = request.tags,
                category = request.category,
                title = request.title,
                description = request.description,
                client = member.companyName,
                previewHeight = "400px"
            )
        )
    }
    
    @Transactional
    fun updateAd(adId: Long, memberId: Long, request: AdUpdateRequest): UserAdItem? {
        // 광고 정보 업데이트
        val ad = getAdDetailFromDb(adId, memberId) ?: return null
        
        return UserAdItem(
            id = ad.id,
            roundId = ad.roundId,
            roundTitle = ad.roundTitle,
            status = ad.status,
            adContent = ad.adContent,
            htmlFilePath = ad.htmlFilePath,
            previewUrl = ad.previewUrl,
            createdAt = ad.createdAt,
            updatedAt = LocalDateTime.now(),
            completedAt = ad.completedAt,
            errorMessage = ad.errorMessage,
            retryCount = ad.retryCount,
            metadata = AdMetadata(
                tags = request.tags ?: ad.metadata?.tags,
                category = request.category ?: ad.metadata?.category,
                title = request.title ?: ad.metadata?.title,
                description = request.description ?: ad.metadata?.description,
                client = ad.metadata?.client,
                previewHeight = ad.metadata?.previewHeight
            )
        )
    }
    
    @Transactional
    fun deleteAd(adId: Long, memberId: Long): Boolean {
        // 광고 삭제
        val ad = getAdDetailFromDb(adId, memberId) ?: return false
        
        // 실제로는 DB에서 삭제 또는 상태 변경
        return true
    }
    
    @Transactional
    fun retryAdGeneration(adId: Long, memberId: Long): UserAdItem? {
        // 광고 생성 재시도
        val ad = getAdDetailFromDb(adId, memberId) ?: return null
        
        if (ad.status != AdStatus.FAILED) {
            throw IllegalArgumentException("실패한 광고만 재시도할 수 있습니다.")
        }
        
        // 실제로는 광고 생성 서비스 재호출
        return UserAdItem(
            id = ad.id,
            roundId = ad.roundId,
            roundTitle = ad.roundTitle,
            status = AdStatus.PENDING,
            adContent = ad.adContent,
            htmlFilePath = ad.htmlFilePath,
            previewUrl = ad.previewUrl,
            createdAt = ad.createdAt,
            updatedAt = LocalDateTime.now(),
            completedAt = null,
            errorMessage = null,
            retryCount = ad.retryCount + 1,
            metadata = ad.metadata
        )
    }
    
    private fun getUserAdsFromDb(memberId: Long, status: AdStatus?, limit: Int): List<UserAdItem> {
        // 실제 DB 조회 로직 - 임시 데이터
        val allAds = listOf(
            UserAdItem(
                id = 15,
                roundId = 16,
                roundTitle = "연말 Q4 라운드 #1",
                status = AdStatus.COMPLETED,
                adContent = "<!DOCTYPE html><html>...</html>",
                htmlFilePath = "generated_ads/round_16_member_15_1696723800.html",
                previewUrl = "/ads/generated_ads/round_16_member_15_1696723800.html",
                createdAt = LocalDateTime.of(2024, 10, 7, 13, 30),
                updatedAt = LocalDateTime.of(2024, 10, 7, 13, 35),
                completedAt = LocalDateTime.of(2024, 10, 7, 13, 35),
                errorMessage = null,
                retryCount = 0,
                metadata = AdMetadata(
                    tags = listOf("헬스케어", "브랜딩"),
                    category = "헬스케어",
                    title = "헬스케어 브랜드 광고",
                    description = "건강한 라이프스타일을 제안하는 브랜드 광고",
                    client = "테스트 회사",
                    previewHeight = "400px"
                )
            ),
            UserAdItem(
                id = 16,
                roundId = 16,
                roundTitle = "연말 Q4 라운드 #1",
                status = AdStatus.PENDING,
                adContent = null,
                htmlFilePath = null,
                previewUrl = null,
                createdAt = LocalDateTime.of(2024, 10, 7, 13, 30),
                updatedAt = LocalDateTime.of(2024, 10, 7, 13, 30),
                completedAt = null,
                errorMessage = null,
                retryCount = 0,
                metadata = AdMetadata(
                    tags = listOf("금융", "투자"),
                    category = "금융",
                    title = "투자 상품 광고",
                    description = "안전한 투자 상품을 소개하는 광고",
                    client = "ABC 주식회사",
                    previewHeight = "350px"
                )
            ),
            UserAdItem(
                id = 17,
                roundId = 15,
                roundTitle = "가을 시즌 라운드",
                status = AdStatus.PUBLISHED,
                adContent = "<!DOCTYPE html><html>...</html>",
                htmlFilePath = "generated_ads/round_15_member_${memberId}_1696620000.html",
                previewUrl = "/ads/generated_ads/round_15_member_${memberId}_1696620000.html",
                createdAt = LocalDateTime.of(2024, 10, 6, 10, 0),
                updatedAt = LocalDateTime.of(2024, 10, 6, 10, 30),
                completedAt = LocalDateTime.of(2024, 10, 6, 10, 30),
                errorMessage = null,
                retryCount = 0,
                metadata = AdMetadata(
                    tags = listOf("교육", "온라인"),
                    category = "교육",
                    title = "온라인 교육 플랫폼",
                    description = "혁신적인 온라인 학습 경험을 제공하는 교육 플랫폼",
                    client = "에듀테크 회사",
                    previewHeight = "450px"
                )
            )
        )
        
        val filteredAds = if (status != null) {
            allAds.filter { it.status == status }
        } else {
            allAds
        }
        
        return filteredAds.take(limit)
    }
    
    private fun calculateStatusCounts(memberId: Long): AdStatusCounts {
        val ads = getUserAdsFromDb(memberId, null, 1000)
        return AdStatusCounts(
            pending = ads.count { it.status == AdStatus.PENDING },
            inProgress = ads.count { it.status == AdStatus.IN_PROGRESS },
            completed = ads.count { it.status == AdStatus.COMPLETED },
            failed = ads.count { it.status == AdStatus.FAILED },
            published = ads.count { it.status == AdStatus.PUBLISHED }
        )
    }
    
    private fun getAdDetailFromDb(adId: Long, memberId: Long): AdDetailResponse? {
        // 실제 DB에서 광고 상세 조회
        return AdDetailResponse(
            id = adId,
            roundId = 16,
            roundTitle = "연말 Q4 라운드 #1",
            memberId = memberId,
            memberCompanyName = "테스트 회사",
            memberEmail = "test@company.com",
            status = AdStatus.COMPLETED,
            adContent = "<!DOCTYPE html><html>...</html>",
            htmlFilePath = "generated_ads/round_16_member_${memberId}_${adId}.html",
            previewUrl = "/ads/generated_ads/round_16_member_${memberId}_${adId}.html",
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now().minusDays(1).plusMinutes(5),
            completedAt = LocalDateTime.now().minusDays(1).plusMinutes(5),
            errorMessage = null,
            retryCount = 0,
            metadata = AdMetadata(
                tags = listOf("헬스케어", "브랜딩"),
                category = "헬스케어",
                title = "헬스케어 브랜드 광고",
                description = "건강한 라이프스타일을 제안하는 브랜드 광고",
                client = "테스트 회사",
                previewHeight = "400px"
            ),
            performance = AdPerformance(
                views = 1250,
                clicks = 87,
                ctr = 6.96,
                revenue = 125000.0
            )
        )
    }
    
    private fun getPublicAdsFromDb(category: String?, limit: Int): List<AdGalleryItem> {
        // 공개 광고 갤러리 데이터
        return listOf(
            AdGalleryItem(
                id = 1,
                title = "스마트 홈 IoT 솔루션",
                description = "미래형 스마트 홈 기술을 소개하는 인터랙티브 광고",
                htmlFilePath = "smart-home-iot.html",
                previewUrl = "/ads/smart-home-iot.html",
                tags = listOf("IoT", "스마트홈", "기술"),
                category = "기술",
                client = "테크 이노베이션",
                createdAt = LocalDateTime.now().minusDays(5),
                previewHeight = "500px",
                isPublic = true
            ),
            AdGalleryItem(
                id = 2,
                title = "친환경 화장품 브랜드",
                description = "자연과 함께하는 뷰티 브랜드의 지속가능한 가치를 전달",
                htmlFilePath = "eco-beauty-brand.html",
                previewUrl = "/ads/eco-beauty-brand.html",
                tags = listOf("화장품", "친환경", "뷰티"),
                category = "뷰티",
                client = "그린 뷰티",
                createdAt = LocalDateTime.now().minusDays(3),
                previewHeight = "450px",
                isPublic = true
            )
        )
    }
    
    private fun getAllCategories(): List<String> {
        return listOf(
            "헬스케어", "금융", "교육", "기술", "뷰티", "부동산", 
            "자동차", "여행", "음식", "패션", "스포츠", "엔터테인먼트"
        )
    }
}