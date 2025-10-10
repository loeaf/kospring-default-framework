-- =====================================================
-- 순환발주 기반 광고 소개 블로그 시스템 
-- 최종 DDL (Final Version)
-- =====================================================

-- =====================================================
-- 1. 테이블 생성
-- =====================================================

-- 순환발주 광고 소개 게시글 테이블
CREATE TABLE posts (
    -- 기본 정보
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시글 ID',
    title VARCHAR(200) NOT NULL COMMENT '제목',
    content TEXT COMMENT '내용',
    
    -- 연관관계
    author_id BIGINT NOT NULL COMMENT '작성자 ID (members.id 참조)',
    round_id BIGINT NOT NULL COMMENT '라운드 ID (rounds.id 참조)',
    target_ad_task_id BIGINT NOT NULL COMMENT '소개 대상 광고 ID (ad_tasks.id 참조)',
    
    -- 순환발주 관련
    assigned_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '할당된 비용 (순환발주로 계산된 금액)',
    
    -- 게시글 속성
    post_type ENUM('BLOG_INTRODUCTION', 'REVIEW', 'TUTORIAL', 'SHOWCASE') NOT NULL DEFAULT 'BLOG_INTRODUCTION' COMMENT '게시글 유형',
    status ENUM('DRAFT', 'PUBLISHED', 'HIDDEN') NOT NULL DEFAULT 'DRAFT' COMMENT '게시글 상태',
    
    -- 게시 일정 (라운드와 동일)
    post_start_date DATETIME NOT NULL COMMENT '게시 시작일시 (라운드 시작일시와 동일)',
    post_end_date DATETIME NOT NULL COMMENT '게시 종료일시 (라운드 종료일시와 동일)',
    
    -- 시간 정보
    published_at DATETIME NULL COMMENT '발행일시',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    -- =====================================================
    -- 2. 제약조건
    -- =====================================================
    
    -- 비용 양수 제약
    CONSTRAINT chk_assigned_cost CHECK (assigned_cost >= 0),
    
    -- 게시 기간 유효성 제약
    CONSTRAINT chk_post_dates CHECK (post_end_date > post_start_date),
    
    -- 중복 방지: 같은 라운드에서 같은 광고에 대해 한 사용자는 하나의 게시글만 작성 가능
    UNIQUE KEY uk_posts_author_ad_round (author_id, target_ad_task_id, round_id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='순환발주 광고 소개 게시글 테이블';

-- =====================================================
-- 3. 인덱스 생성
-- =====================================================

-- 기본 조회용 인덱스
CREATE INDEX idx_posts_round_id ON posts (round_id);
CREATE INDEX idx_posts_target_ad_task ON posts (target_ad_task_id);
CREATE INDEX idx_posts_author_id ON posts (author_id);
CREATE INDEX idx_posts_status ON posts (status);
CREATE INDEX idx_posts_post_type ON posts (post_type);

-- 정렬용 인덱스
CREATE INDEX idx_posts_assigned_cost ON posts (assigned_cost DESC);
CREATE INDEX idx_posts_created_at ON posts (created_at DESC);

-- 게시 기간 관련 인덱스
CREATE INDEX idx_posts_active_period ON posts (post_start_date, post_end_date);

-- 복합 인덱스
CREATE INDEX idx_posts_round_status ON posts (round_id, status);

-- 전문 검색 인덱스
ALTER TABLE posts ADD FULLTEXT INDEX idx_posts_search (title, content);

-- =====================================================
-- 4. 외래키 제약조건
-- =====================================================

-- 작성자 참조
ALTER TABLE posts ADD CONSTRAINT fk_posts_author 
    FOREIGN KEY (author_id) REFERENCES members(id) ON DELETE CASCADE;

-- 라운드 참조
ALTER TABLE posts ADD CONSTRAINT fk_posts_round 
    FOREIGN KEY (round_id) REFERENCES rounds(id) ON DELETE CASCADE;

-- 대상 광고 참조
ALTER TABLE posts ADD CONSTRAINT fk_posts_target_ad 
    FOREIGN KEY (target_ad_task_id) REFERENCES ad_tasks(id) ON DELETE CASCADE;

-- =====================================================
-- 5. 관리용 뷰 (Views)
-- =====================================================

-- 광고별 게시글 통계
CREATE VIEW ad_post_stats AS
SELECT 
    at.id as ad_task_id,
    at.round_id,
    COUNT(p.id) as post_count,
    SUM(CASE WHEN p.status = 'PUBLISHED' THEN p.assigned_cost ELSE 0 END) as total_assigned_cost,
    AVG(CASE WHEN p.status = 'PUBLISHED' THEN p.assigned_cost END) as avg_cost_per_post,
    COUNT(CASE WHEN p.status = 'PUBLISHED' THEN 1 END) as published_posts,
    COUNT(CASE WHEN p.status = 'DRAFT' THEN 1 END) as draft_posts
FROM ad_tasks at
LEFT JOIN posts p ON at.id = p.target_ad_task_id
GROUP BY at.id, at.round_id;

-- 라운드별 게시글 통계 (게시 일정 포함)
CREATE VIEW round_post_stats AS
SELECT 
    r.id as round_id,
    r.title as round_title,
    r.start_date as post_start_date,
    r.end_date as post_end_date,
    CASE 
        WHEN NOW() < r.start_date THEN '시작 전'
        WHEN NOW() BETWEEN r.start_date AND r.end_date THEN '진행 중'
        ELSE '종료'
    END as post_period_status,
    COUNT(p.id) as total_posts,
    COUNT(CASE WHEN p.status = 'PUBLISHED' THEN 1 END) as published_posts,
    SUM(CASE WHEN p.status = 'PUBLISHED' THEN p.assigned_cost ELSE 0 END) as total_assigned_cost,
    COUNT(DISTINCT p.author_id) as unique_authors,
    COUNT(DISTINCT p.target_ad_task_id) as covered_ads
FROM rounds r
LEFT JOIN posts p ON r.id = p.round_id
GROUP BY r.id, r.title, r.start_date, r.end_date;

-- 순환발주 밸런스 체크 (핵심 검증 뷰)
CREATE VIEW circular_balance_check AS
SELECT 
    r.id as round_id,
    r.title as round_title,
    m.id as member_id,
    m.company_name,
    -- 이 멤버가 작성해서 받을 금액
    COALESCE(SUM(CASE WHEN p.author_id = m.id AND p.status = 'PUBLISHED' THEN p.assigned_cost END), 0) as amount_to_receive,
    -- 이 멤버의 광고에 대한 게시글 총 비용 (지불할 금액)  
    COALESCE(SUM(CASE WHEN at.member_id = m.id AND p.status = 'PUBLISHED' THEN p.assigned_cost END), 0) as amount_to_pay,
    -- 밸런스 (0이어야 완벽한 순환발주)
    COALESCE(SUM(CASE WHEN p.author_id = m.id AND p.status = 'PUBLISHED' THEN p.assigned_cost END), 0) - 
    COALESCE(SUM(CASE WHEN at.member_id = m.id AND p.status = 'PUBLISHED' THEN p.assigned_cost END), 0) as balance
FROM rounds r
CROSS JOIN members m
LEFT JOIN posts p ON r.id = p.round_id
LEFT JOIN ad_tasks at ON p.target_ad_task_id = at.id
GROUP BY r.id, r.title, m.id, m.company_name
HAVING amount_to_receive > 0 OR amount_to_pay > 0;

-- 라운드 완성도 체크 (n명 참여시 n-1개 게시글 작성 여부)
CREATE VIEW round_completion_status AS
SELECT 
    r.id as round_id,
    r.title as round_title,
    COUNT(DISTINCT o.member_id) as total_participants,
    COUNT(DISTINCT o.member_id) - 1 as required_posts_per_participant,
    (COUNT(DISTINCT o.member_id) - 1) * COUNT(DISTINCT o.member_id) as total_required_posts,
    COUNT(p.id) as actual_published_posts,
    ROUND(
        (COUNT(p.id) / ((COUNT(DISTINCT o.member_id) - 1) * COUNT(DISTINCT o.member_id))) * 100, 2
    ) as completion_percentage,
    CASE 
        WHEN COUNT(p.id) >= (COUNT(DISTINCT o.member_id) - 1) * COUNT(DISTINCT o.member_id) 
        THEN '완성' 
        ELSE '미완성' 
    END as completion_status
FROM rounds r
JOIN orders o ON r.id = o.round_id  
LEFT JOIN posts p ON r.id = p.round_id AND p.status = 'PUBLISHED'
GROUP BY r.id, r.title;

-- 현재 활성 게시글 뷰 (게시 기간 중)
CREATE VIEW active_posts AS
SELECT p.*, 
       m.company_name as author_name,
       at.ad_content as target_ad_content
FROM posts p
JOIN members m ON p.author_id = m.id
JOIN ad_tasks at ON p.target_ad_task_id = at.id
WHERE p.status = 'PUBLISHED'
  AND NOW() BETWEEN p.post_start_date AND p.post_end_date;

-- =====================================================
-- 6. 샘플 데이터
-- =====================================================

-- 라운드 1 (2024-01-01 ~ 2024-01-31)의 순환발주 게시글들
INSERT INTO posts (
    title, content, author_id, round_id, target_ad_task_id, assigned_cost, 
    post_type, status, post_start_date, post_end_date, published_at
) VALUES
-- 작성자 1 → 광고 2 소개 (2500만원)
('AI 마케팅 혁신 솔루션 - 스마트애드', '
# 스마트애드: 차세대 AI 마케팅 플랫폼

같은 라운드에서 함께하는 스마트애드의 혁신적인 AI 마케팅 솔루션을 소개합니다.

## 🎯 핵심 기능
- **정밀 타겟팅**: 머신러닝 기반 고객 분석
- **실시간 최적화**: 캠페인 성과 자동 개선  
- **ROI 극대화**: 평균 300% 성과 향상

## 📈 검증된 성과
- 클릭률 250% 증가
- 전환율 180% 향상  
- 고객 만족도 95% 달성

순환발주 파트너로서 적극 추천드립니다!

---
*이 게시글은 순환발주 시스템에 따라 작성된 광고 소개글입니다.*
', 1, 1, 2, 2500.00, 'BLOG_INTRODUCTION', 'PUBLISHED', 
'2024-01-01 00:00:00', '2024-01-31 23:59:59', NOW()),

-- 작성자 2 → 광고 3 소개 (2500만원)
('이커머스 플랫폼의 미래 - 샵메이트', '
# 샵메이트: 온라인 쇼핑의 새로운 패러다임

혁신적인 이커머스 솔루션 샵메이트를 소개합니다.

## 🛒 주요 특징
### AI 개인화 추천
- 고객 행동 패턴 분석
- 맞춤형 상품 제안

### 통합 관리 시스템  
- 재고, 주문, 배송 원스톱
- 다채널 판매 지원

## 🚀 도입 효과
- 매출 40% 증가
- 관리시간 60% 단축
- 고객만족도 4.8/5.0

중소 쇼핑몰도 대기업 수준 시스템 구축이 가능합니다!
', 2, 1, 3, 2500.00, 'BLOG_INTRODUCTION', 'PUBLISHED',
'2024-01-01 00:00:00', '2024-01-31 23:59:59', NOW()),

-- 작성자 3 → 광고 4 소개 (2500만원)
('클라우드 보안의 새로운 기준 - 시큐어클라우드', '
# 시큐어클라우드: 기업 데이터 보호 최전선

디지털 전환 시대 필수 보안 솔루션을 소개합니다.

## 🔒 핵심 기술
### Zero Trust 아키텍처
- 모든 접근 검증
- 포괄적 보안 모델

### AI 위협 탐지
- 실시간 이상 행동 분석
- 99.9% 정확도 달성

## 💼 맞춤 솔루션
- **스타트업**: 기본 보안 패키지
- **중견기업**: 확장 가능 인프라  
- **대기업**: 엔터프라이즈급 통합 보안

검증된 성과: 보안사고 99% 감소!
', 3, 1, 4, 2500.00, 'BLOG_INTRODUCTION', 'PUBLISHED',
'2024-01-01 00:00:00', '2024-01-31 23:59:59', NOW()),

-- 임시저장 상태 게시글 (작성 중)
('작성 중인 앱 개발 솔루션 소개', '
앱크리에이터의 혁신적인 노코드 앱 개발 플랫폼을 소개할 예정입니다.

## 주요 내용 (작성 예정)
- 드래그 앤 드롭 빌더
- iOS/Android 동시 출시
- 성공 사례들
- 비용 효율성

상세한 내용을 추가 작성 중입니다...
', 4, 1, 5, 2500.00, 'BLOG_INTRODUCTION', 'DRAFT',
'2024-01-01 00:00:00', '2024-01-31 23:59:59', NULL);

-- =====================================================
-- 7. 유용한 관리 쿼리들
-- =====================================================

/*
-- 현재 활성 중인 게시글 조회
SELECT title, author_name, target_ad_content, assigned_cost, post_start_date, post_end_date
FROM active_posts
ORDER BY assigned_cost DESC;

-- 라운드별 완성도 현황
SELECT round_title, total_participants, required_posts_per_participant, 
       actual_published_posts, completion_percentage, completion_status
FROM round_completion_status
ORDER BY completion_percentage DESC;

-- 순환발주 밸런스 체크 (불균형 발견)
SELECT round_title, company_name, amount_to_receive, amount_to_pay, balance
FROM circular_balance_check 
WHERE ABS(balance) > 0.01
ORDER BY ABS(balance) DESC;

-- 게시 기간별 현황
SELECT round_title, post_period_status, total_posts, published_posts, 
       total_assigned_cost, post_start_date, post_end_date
FROM round_post_stats
ORDER BY post_start_date DESC;

-- 비용 할당이 높은 게시글 순위
SELECT p.title, m.company_name as author, at.ad_content as target_ad,
       p.assigned_cost, p.status, p.post_start_date, p.post_end_date
FROM posts p
JOIN members m ON p.author_id = m.id  
JOIN ad_tasks at ON p.target_ad_task_id = at.id
WHERE p.status = 'PUBLISHED'
ORDER BY p.assigned_cost DESC
LIMIT 10;
*/

-- =====================================================
-- DDL 완료
-- =====================================================