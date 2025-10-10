-- 라운드 기반 광고 후기 게시판 DDL

-- 기존 테이블이 있다면 삭제 (개발 환경용)
-- DROP TABLE IF EXISTS posts;

-- 광고 후기 게시글 테이블
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '후기 게시글 ID',
    title VARCHAR(200) NOT NULL COMMENT '후기 제목',
    content TEXT COMMENT '후기 내용',
    author_id BIGINT NOT NULL COMMENT '후기 작성자 ID (members.id 참조)',
    round_id BIGINT NOT NULL COMMENT '라운드 ID (rounds.id 참조)',
    target_ad_task_id BIGINT NOT NULL COMMENT '후기 대상 광고 ID (ad_tasks.id 참조)',
    rating INT NOT NULL DEFAULT 5 COMMENT '평점 (1-5점)',
    status ENUM('DRAFT', 'PUBLISHED', 'HIDDEN') NOT NULL DEFAULT 'DRAFT' COMMENT '게시글 상태',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT '조회수',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE COMMENT '추천 여부',
    featured_until DATETIME NULL COMMENT '추천 종료일시',
    published_at DATETIME NULL COMMENT '발행일시',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    -- 제약조건
    CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5),
    
    -- 같은 라운드에서 같은 광고에 대해 한 사용자는 하나의 후기만 작성 가능
    UNIQUE KEY uk_posts_author_ad (author_id, target_ad_task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='광고 후기 게시글 테이블';

-- 인덱스 생성
CREATE INDEX idx_posts_round_id ON posts (round_id);
CREATE INDEX idx_posts_target_ad_task ON posts (target_ad_task_id);
CREATE INDEX idx_posts_author_id ON posts (author_id);
CREATE INDEX idx_posts_status ON posts (status);
CREATE INDEX idx_posts_rating ON posts (rating DESC);
CREATE INDEX idx_posts_created_at ON posts (created_at DESC);
CREATE INDEX idx_posts_view_count ON posts (view_count DESC);
CREATE INDEX idx_posts_featured ON posts (is_featured, featured_until);
CREATE INDEX idx_posts_status_created ON posts (status, created_at DESC);
CREATE INDEX idx_posts_round_status ON posts (round_id, status);

-- 전문 검색 인덱스
ALTER TABLE posts ADD FULLTEXT INDEX idx_posts_search (title, content);

-- 외래키 제약조건
ALTER TABLE posts ADD CONSTRAINT fk_posts_author 
    FOREIGN KEY (author_id) REFERENCES members(id) ON DELETE CASCADE;

ALTER TABLE posts ADD CONSTRAINT fk_posts_round 
    FOREIGN KEY (round_id) REFERENCES rounds(id) ON DELETE CASCADE;

ALTER TABLE posts ADD CONSTRAINT fk_posts_target_ad 
    FOREIGN KEY (target_ad_task_id) REFERENCES ad_tasks(id) ON DELETE CASCADE;

-- 광고별 평점 통계 뷰
CREATE VIEW ad_rating_stats AS
SELECT 
    at.id as ad_task_id,
    at.round_id,
    COUNT(p.id) as review_count,
    AVG(p.rating) as avg_rating,
    SUM(CASE WHEN p.rating = 5 THEN 1 ELSE 0 END) as five_star_count,
    SUM(CASE WHEN p.rating = 4 THEN 1 ELSE 0 END) as four_star_count,
    SUM(CASE WHEN p.rating = 3 THEN 1 ELSE 0 END) as three_star_count,
    SUM(CASE WHEN p.rating = 2 THEN 1 ELSE 0 END) as two_star_count,
    SUM(CASE WHEN p.rating = 1 THEN 1 ELSE 0 END) as one_star_count,
    SUM(p.view_count) as total_views
FROM ad_tasks at
LEFT JOIN posts p ON at.id = p.target_ad_task_id AND p.status = 'PUBLISHED'
GROUP BY at.id, at.round_id;

-- 라운드별 후기 통계 뷰
CREATE VIEW round_review_stats AS
SELECT 
    r.id as round_id,
    r.title as round_title,
    COUNT(p.id) as total_reviews,
    COUNT(CASE WHEN p.status = 'PUBLISHED' THEN 1 END) as published_reviews,
    AVG(CASE WHEN p.status = 'PUBLISHED' THEN p.rating END) as avg_rating,
    COUNT(DISTINCT p.author_id) as unique_reviewers,
    COUNT(DISTINCT p.target_ad_task_id) as reviewed_ads
FROM rounds r
LEFT JOIN posts p ON r.id = p.round_id
GROUP BY r.id, r.title;

-- 샘플 데이터 삽입 (개발/테스트용)
-- 라운드 1에서 사용자 1이 사용자 2의 광고(ad_task_id=1)에 대한 후기
INSERT INTO posts (title, content, author_id, round_id, target_ad_task_id, rating, status, published_at) VALUES
('정말 창의적인 광고였어요!', '
같은 라운드에 참여하면서 이 광고를 보고 정말 감탄했습니다.

## 인상깊었던 점
- 독창적인 아이디어
- 깔끔한 디자인
- 타겟팅이 정확함

저희 비즈니스에도 참고하고 싶은 부분이 많았습니다. 
다음 라운드에서도 함께 하고 싶네요!
', 1, 1, 1, 5, 'PUBLISHED', NOW()),

('효과적인 마케팅 전략', '
이번 라운드에서 본 광고 중 가장 임팩트 있었던 것 같습니다.

### 좋았던 점
1. 명확한 메시지 전달
2. 시각적 완성도
3. 고객 반응도 좋았음

### 아쉬운 점
- 초기 로딩이 조금 느렸음

전체적으로는 매우 만족스럽습니다.
', 2, 1, 2, 4, 'PUBLISHED', NOW()),

('기대 이상의 퀄리티', '
처음에는 반신반의했는데, 결과물을 보니 정말 놀랐습니다.

특히 AI 생성 부분이 자연스러워서 좋았어요.
우리 업계에서는 이런 접근이 참신했습니다.

다른 분들께도 추천하고 싶어요!
', 1, 1, 3, 5, 'PUBLISHED', NOW()),

('아직 작성 중인 후기', '
라운드가 끝나고 후기를 정리하고 있습니다.

광고 효과 분석이 끝나면 상세히 작성하겠습니다.
', 3, 1, 4, 4, 'DRAFT', NULL),

('관리자가 숨긴 후기', '
부적절한 내용이 포함된 후기입니다.
', 2, 1, 5, 2, 'HIDDEN', NOW());

-- 유용한 쿼리 예시들

-- 특정 라운드의 모든 후기 조회
-- SELECT p.*, m.company_name as author_name, at.ad_content 
-- FROM posts p
-- JOIN members m ON p.author_id = m.id
-- JOIN ad_tasks at ON p.target_ad_task_id = at.id
-- WHERE p.round_id = 1 AND p.status = 'PUBLISHED'
-- ORDER BY p.created_at DESC;

-- 특정 광고에 대한 모든 후기와 평점 조회
-- SELECT p.*, m.company_name as reviewer_name
-- FROM posts p
-- JOIN members m ON p.author_id = m.id
-- WHERE p.target_ad_task_id = 1 AND p.status = 'PUBLISHED'
-- ORDER BY p.rating DESC, p.created_at DESC;

-- 평점 높은 광고 순으로 조회
-- SELECT at.id, at.ad_content, AVG(p.rating) as avg_rating, COUNT(p.id) as review_count
-- FROM ad_tasks at
-- JOIN posts p ON at.id = p.target_ad_task_id
-- WHERE at.round_id = 1 AND p.status = 'PUBLISHED'
-- GROUP BY at.id, at.ad_content
-- HAVING review_count >= 2
-- ORDER BY avg_rating DESC, review_count DESC;

-- 작성자별 후기 작성 현황
-- SELECT m.company_name, COUNT(p.id) as total_reviews, AVG(p.rating) as avg_given_rating
-- FROM members m
-- LEFT JOIN posts p ON m.id = p.author_id AND p.status = 'PUBLISHED'
-- GROUP BY m.id, m.company_name
-- ORDER BY total_reviews DESC;