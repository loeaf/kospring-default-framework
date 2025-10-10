-- 순환발주 기반 광고 소개 블로그 시스템 DDL

-- 기존 테이블이 있다면 삭제 (개발 환경용)
-- DROP TABLE IF EXISTS posts;

-- 순환발주 광고 소개 게시글 테이블
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시글 ID',
    title VARCHAR(200) NOT NULL COMMENT '제목',
    content TEXT COMMENT '내용',
    author_id BIGINT NOT NULL COMMENT '작성자 ID (members.id 참조)',
    round_id BIGINT NOT NULL COMMENT '라운드 ID (rounds.id 참조)',
    target_ad_task_id BIGINT NOT NULL COMMENT '소개 대상 광고 ID (ad_tasks.id 참조)',
    assigned_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '할당된 비용 (순환발주로 계산된 금액)',
    post_type ENUM('BLOG_INTRODUCTION', 'REVIEW', 'TUTORIAL', 'SHOWCASE') NOT NULL DEFAULT 'BLOG_INTRODUCTION' COMMENT '게시글 유형',
    status ENUM('DRAFT', 'PUBLISHED', 'HIDDEN') NOT NULL DEFAULT 'DRAFT' COMMENT '게시글 상태',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT '조회수',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE COMMENT '추천 여부',
    featured_until DATETIME NULL COMMENT '추천 종료일시',
    published_at DATETIME NULL COMMENT '발행일시',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    -- 제약조건
    CONSTRAINT chk_assigned_cost CHECK (assigned_cost >= 0),
    
    -- 같은 라운드에서 같은 광고에 대해 한 사용자는 하나의 게시글만 작성 가능
    UNIQUE KEY uk_posts_author_ad_round (author_id, target_ad_task_id, round_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='순환발주 광고 소개 게시글 테이블';

-- 인덱스 생성
CREATE INDEX idx_posts_round_id ON posts (round_id);
CREATE INDEX idx_posts_target_ad_task ON posts (target_ad_task_id);
CREATE INDEX idx_posts_author_id ON posts (author_id);
CREATE INDEX idx_posts_status ON posts (status);
CREATE INDEX idx_posts_post_type ON posts (post_type);
CREATE INDEX idx_posts_assigned_cost ON posts (assigned_cost DESC);
CREATE INDEX idx_posts_created_at ON posts (created_at DESC);
CREATE INDEX idx_posts_view_count ON posts (view_count DESC);
CREATE INDEX idx_posts_featured ON posts (is_featured, featured_until);
CREATE INDEX idx_posts_status_created ON posts (status, created_at DESC);
CREATE INDEX idx_posts_round_status ON posts (round_id, status);
CREATE INDEX idx_posts_round_type ON posts (round_id, post_type);

-- 전문 검색 인덱스
ALTER TABLE posts ADD FULLTEXT INDEX idx_posts_search (title, content);

-- 외래키 제약조건
ALTER TABLE posts ADD CONSTRAINT fk_posts_author 
    FOREIGN KEY (author_id) REFERENCES members(id) ON DELETE CASCADE;

ALTER TABLE posts ADD CONSTRAINT fk_posts_round 
    FOREIGN KEY (round_id) REFERENCES rounds(id) ON DELETE CASCADE;

ALTER TABLE posts ADD CONSTRAINT fk_posts_target_ad 
    FOREIGN KEY (target_ad_task_id) REFERENCES ad_tasks(id) ON DELETE CASCADE;

-- 광고별 게시글 통계 뷰
CREATE VIEW ad_post_stats AS
SELECT 
    at.id as ad_task_id,
    at.round_id,
    COUNT(p.id) as post_count,
    SUM(CASE WHEN p.status = 'PUBLISHED' THEN p.assigned_cost ELSE 0 END) as total_assigned_cost,
    AVG(CASE WHEN p.status = 'PUBLISHED' THEN p.assigned_cost END) as avg_cost_per_post,
    SUM(CASE WHEN p.status = 'PUBLISHED' THEN p.view_count ELSE 0 END) as total_views,
    COUNT(CASE WHEN p.status = 'PUBLISHED' THEN 1 END) as published_posts,
    COUNT(CASE WHEN p.status = 'DRAFT' THEN 1 END) as draft_posts
FROM ad_tasks at
LEFT JOIN posts p ON at.id = p.target_ad_task_id
GROUP BY at.id, at.round_id;

-- 라운드별 게시글 통계 뷰
CREATE VIEW round_post_stats AS
SELECT 
    r.id as round_id,
    r.title as round_title,
    COUNT(p.id) as total_posts,
    COUNT(CASE WHEN p.status = 'PUBLISHED' THEN 1 END) as published_posts,
    SUM(CASE WHEN p.status = 'PUBLISHED' THEN p.assigned_cost ELSE 0 END) as total_assigned_cost,
    COUNT(DISTINCT p.author_id) as unique_authors,
    COUNT(DISTINCT p.target_ad_task_id) as covered_ads,
    AVG(CASE WHEN p.status = 'PUBLISHED' THEN p.assigned_cost END) as avg_cost_per_post
FROM rounds r
LEFT JOIN posts p ON r.id = p.round_id
GROUP BY r.id, r.title;

-- 순환발주 밸런스 체크 뷰 (각 참여자가 지불하고 받는 금액 확인)
CREATE VIEW circular_balance_check AS
SELECT 
    r.id as round_id,
    r.title as round_title,
    m.id as member_id,
    m.company_name,
    -- 이 멤버가 작성해야 할 게시글들의 총 비용 (받아야 할 금액)
    COALESCE(SUM(CASE WHEN p.author_id = m.id AND p.status = 'PUBLISHED' THEN p.assigned_cost END), 0) as amount_to_receive,
    -- 이 멤버의 광고에 대한 게시글들의 총 비용 (지불해야 할 금액)
    COALESCE(SUM(CASE WHEN at.member_id = m.id AND p.status = 'PUBLISHED' THEN p.assigned_cost END), 0) as amount_to_pay,
    -- 밸런스 (이상적으로는 0이어야 함)
    COALESCE(SUM(CASE WHEN p.author_id = m.id AND p.status = 'PUBLISHED' THEN p.assigned_cost END), 0) - 
    COALESCE(SUM(CASE WHEN at.member_id = m.id AND p.status = 'PUBLISHED' THEN p.assigned_cost END), 0) as balance
FROM rounds r
CROSS JOIN members m
LEFT JOIN posts p ON r.id = p.round_id
LEFT JOIN ad_tasks at ON p.target_ad_task_id = at.id
-- 실제 이 라운드에 참여한 멤버들만 (orders나 다른 참여 테이블 기준으로 필터링 필요)
GROUP BY r.id, r.title, m.id, m.company_name
HAVING amount_to_receive > 0 OR amount_to_pay > 0;

-- 게시글 타입별 통계 뷰
CREATE VIEW post_type_stats AS
SELECT 
    post_type,
    COUNT(*) as total_count,
    COUNT(CASE WHEN status = 'PUBLISHED' THEN 1 END) as published_count,
    AVG(CASE WHEN status = 'PUBLISHED' THEN assigned_cost END) as avg_cost,
    SUM(CASE WHEN status = 'PUBLISHED' THEN view_count END) as total_views
FROM posts
GROUP BY post_type;

-- 샘플 데이터 삽입 (개발/테스트용)
-- 라운드 1에서 5명 참여, 순환발주로 계산된 비용으로 서로의 광고 소개
INSERT INTO posts (title, content, author_id, round_id, target_ad_task_id, assigned_cost, post_type, status, published_at) VALUES
-- 작성자 1이 작성자 2의 광고 소개 (2500만원 할당)
('혁신적인 AI 마케팅 솔루션 - 스마트애드의 성공 사례', '
# 스마트애드: AI 기반 타겟 마케팅의 새로운 패러다임

안녕하세요! 이번 라운드에서 함께하게 된 스마트애드의 AI 마케팅 솔루션을 소개해드리고자 합니다.

## 🎯 핵심 기능
- **정밀한 타겟팅**: 머신러닝 기반 고객 분석
- **실시간 최적화**: 캠페인 성과를 실시간으로 개선
- **ROI 극대화**: 평균 300% 성과 향상

## 💡 왜 주목해야 할까요?
최근 디지털 마케팅 시장에서 가장 혁신적인 접근방식을 제시하고 있습니다.
특히 중소기업도 쉽게 적용할 수 있는 사용자 친화적 인터페이스가 인상적입니다.

## 📈 성과 지표
- 클릭률 평균 250% 향상
- 전환율 180% 증가
- 고객 만족도 95% 이상

더 자세한 정보는 [스마트애드 공식 사이트](https://smartad.example.com)에서 확인하실 수 있습니다.

---
*이 게시글은 라운드 참여자로서 동료 기업의 우수한 솔루션을 소개하는 목적으로 작성되었습니다.*
', 1, 1, 2, 2500.00, 'BLOG_INTRODUCTION', 'PUBLISHED', NOW()),

-- 작성자 2가 작성자 3의 광고 소개 (2500만원 할당)
('차세대 이커머스 플랫폼 - 샵메이트가 바꾸는 온라인 쇼핑', '
# 샵메이트: 온라인 쇼핑의 미래를 제시하다

같은 라운드에서 만난 샵메이트의 혁신적인 이커머스 솔루션을 여러분께 소개합니다.

## 🛒 주요 특징
### 1. 개인화된 쇼핑 경험
- AI 추천 시스템으로 맞춤형 상품 제안
- 고객 행동 패턴 분석을 통한 최적화

### 2. 통합 관리 시스템
- 재고, 주문, 배송까지 원스톱 관리
- 다채널 판매 지원 (온라인몰, 마켓플레이스, 오프라인)

### 3. 성장 지원 도구
- 실시간 분석 대시보드
- 마케팅 자동화 기능
- 고객 관리 CRM 내장

## 🚀 도입 효과
실제 도입 기업들의 성과를 보면:
- **매출 증가**: 평균 40% 향상
- **운영 효율성**: 관리 시간 60% 단축
- **고객 만족도**: 4.8/5.0 달성

특히 중소 쇼핑몰에서도 대기업 수준의 시스템을 구축할 수 있다는 점이 가장 큰 장점입니다.

샵메이트와 함께하는 이번 라운드에서 많은 인사이트를 얻고 있습니다!
', 2, 1, 3, 2500.00, 'BLOG_INTRODUCTION', 'PUBLISHED', NOW()),

-- 작성자 3이 작성자 4의 광고 소개 (2500만원 할당)
('클라우드 보안의 새로운 기준 - 시큐어클라우드', '
# 시큐어클라우드: 기업 데이터 보호의 최전선

디지털 전환 시대, 보안은 선택이 아닌 필수입니다. 
이번 라운드 파트너인 시큐어클라우드의 혁신적인 보안 솔루션을 소개합니다.

## 🔒 핵심 보안 기술

### Zero Trust 아키텍처
- 모든 접근을 검증하는 보안 모델
- 내외부 구분 없는 포괄적 보안

### AI 기반 위협 탐지
- 실시간 이상 행동 패턴 분석
- 99.9% 정확도의 위협 탐지

### 통합 보안 관리
- 단일 콘솔에서 모든 보안 요소 관리
- 자동화된 대응 시스템

## 💼 기업별 맞춤 솔루션

**스타트업**: 기본 보안부터 차근차근
**중견기업**: 확장 가능한 보안 인프라
**대기업**: 엔터프라이즈급 통합 보안

## 📊 검증된 성과
- 보안 사고 99% 감소
- 컴플라이언스 100% 충족
- 관리 비용 50% 절감

클라우드 시대의 필수 파트너로 강력 추천합니다!
', 3, 1, 4, 2500.00, 'BLOG_INTRODUCTION', 'PUBLISHED', NOW()),

-- 작성자 4가 작성자 5의 광고 소개 (2500만원 할당)
('모바일 앱 개발의 혁신 - 앱크리에이터', '
# 앱크리에이터: 누구나 쉽게 만드는 프로급 모바일 앱

코딩 없이도 전문가 수준의 모바일 앱을 만들 수 있다면?
앱크리에이터가 그 꿈을 현실로 만들어줍니다.

## 🎨 직관적인 드래그 앤 드롭 빌더

복잡한 코딩 없이도:
- UI/UX 컴포넌트를 자유롭게 배치
- 실시간 미리보기로 즉시 확인
- 반응형 디자인 자동 적용

## 📱 다양한 플랫폼 지원

### iOS & Android 동시 출시
- 한 번 개발로 양쪽 플랫폼 커버
- 네이티브 성능 보장
- 앱스토어 배포 지원

### PWA 옵션
- 웹에서도 앱처럼 동작
- 별도 설치 없이 사용 가능

## 🚀 성공 사례들

**카페 체인**: 주문 앱으로 매출 30% 증가
**피트니스**: 회원 관리 앱으로 운영 효율성 개선
**교육 기관**: 학습 앱으로 수강생 만족도 향상

## 💡 특별한 장점

1. **빠른 개발**: 평균 2주 만에 앱 완성
2. **저렴한 비용**: 외주 개발 대비 80% 절약
3. **지속적 업데이트**: 시장 변화에 빠른 대응

앱 개발이 필요한 모든 분께 적극 추천드립니다!
', 4, 1, 5, 2500.00, 'BLOG_INTRODUCTION', 'PUBLISHED', NOW()),

-- 작성자 5가 작성자 1의 광고 소개 (2500만원 할당)
('데이터 분석의 새로운 차원 - 애널리틱스프로', '
# 애널리틱스프로: 데이터에서 인사이트까지

빅데이터 시대, 단순한 수집을 넘어 진짜 인사이트가 필요합니다.
애널리틱스프로와 함께하며 느낀 데이터 분석의 새로운 가능성을 공유합니다.

## 📊 고급 분석 기능

### 예측 분석
- 머신러닝 기반 트렌드 예측
- 리스크 사전 감지
- 기회 발굴 자동화

### 실시간 대시보드
- 핵심 KPI 실시간 모니터링
- 알림 및 자동 리포트
- 모바일 최적화 지원

### 고급 시각화
- 인터랙티브 차트와 그래프
- 드릴다운 분석 지원
- 프레젠테이션 모드

## 🎯 업종별 특화 솔루션

**제조업**: 품질 관리 및 예측 정비
**리테일**: 고객 행동 분석 및 재고 최적화
**금융**: 리스크 관리 및 포트폴리오 분석
**헬스케어**: 환자 데이터 분석 및 치료 최적화

## 💼 도입 성과

- 의사결정 속도 70% 향상
- 운영 비용 25% 절감
- 고객 만족도 15% 개선

데이터 기반 경영을 위한 최고의 파트너입니다!
', 5, 1, 1, 2500.00, 'BLOG_INTRODUCTION', 'PUBLISHED', NOW()),

-- 임시저장 상태의 게시글
('작성 중인 블로그 - 핀테크 솔루션 소개', '
아직 작성 중인 내용입니다.

핀테크 솔루션의 혁신적인 기능들을 소개할 예정입니다.
- 간편 결제 시스템
- 투자 포트폴리오 관리
- 개인 자산 관리

더 상세한 내용을 추가하겠습니다.
', 1, 1, 3, 1800.00, 'BLOG_INTRODUCTION', 'DRAFT', NULL);

-- 유용한 쿼리 예시들

-- 특정 라운드의 모든 게시글과 할당 비용 조회
-- SELECT p.*, m.company_name as author_name, at.ad_content, 
--        CONCAT('₩', FORMAT(p.assigned_cost, 0)) as formatted_cost
-- FROM posts p
-- JOIN members m ON p.author_id = m.id
-- JOIN ad_tasks at ON p.target_ad_task_id = at.id
-- WHERE p.round_id = 1 AND p.status = 'PUBLISHED'
-- ORDER BY p.assigned_cost DESC, p.created_at DESC;

-- 순환발주 밸런스 체크 (각 참여자의 수입-지출 균형)
-- SELECT * FROM circular_balance_check 
-- WHERE round_id = 1 
-- ORDER BY ABS(balance) DESC;

-- 라운드별 완성도 체크 (n명 참여시 n-1개 게시글 작성 여부)
-- SELECT r.id, r.title,
--        COUNT(DISTINCT o.member_id) as participants,
--        COUNT(DISTINCT o.member_id) - 1 as required_posts,
--        COUNT(p.id) as actual_posts,
--        CASE 
--            WHEN COUNT(p.id) >= (COUNT(DISTINCT o.member_id) - 1) 
--            THEN '완성' 
--            ELSE '미완성' 
--        END as completion_status
-- FROM rounds r
-- JOIN orders o ON r.id = o.round_id  -- orders 테이블에서 참여자 수 계산
-- LEFT JOIN posts p ON r.id = p.round_id AND p.status = 'PUBLISHED'
-- GROUP BY r.id, r.title;

-- 비용 할당이 가장 높은 게시글들
-- SELECT p.title, m.company_name as author, at.ad_content as target_ad,
--        p.assigned_cost, p.view_count,
--        CONCAT('₩', FORMAT(p.assigned_cost, 0)) as cost_formatted
-- FROM posts p
-- JOIN members m ON p.author_id = m.id  
-- JOIN ad_tasks at ON p.target_ad_task_id = at.id
-- WHERE p.status = 'PUBLISHED'
-- ORDER BY p.assigned_cost DESC
-- LIMIT 10;