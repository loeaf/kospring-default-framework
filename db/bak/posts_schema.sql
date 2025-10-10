-- 게시판 시스템 완전한 DDL

-- 기존 테이블이 있다면 삭제 (개발 환경용)
-- DROP TABLE IF EXISTS posts;

-- 게시글 테이블
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시글 ID',
    title VARCHAR(200) NOT NULL COMMENT '제목',
    content TEXT COMMENT '내용',
    author_id BIGINT NOT NULL COMMENT '작성자 ID (members.id 참조)',
    status ENUM('DRAFT', 'PUBLISHED', 'HIDDEN') NOT NULL DEFAULT 'DRAFT' COMMENT '게시글 상태',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT '조회수',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE COMMENT '추천 여부',
    featured_until DATETIME NULL COMMENT '추천 종료일시',
    published_at DATETIME NULL COMMENT '발행일시',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 테이블';

-- 인덱스 생성
CREATE INDEX idx_posts_status ON posts (status);
CREATE INDEX idx_posts_author_id ON posts (author_id);
CREATE INDEX idx_posts_created_at ON posts (created_at DESC);
CREATE INDEX idx_posts_view_count ON posts (view_count DESC);
CREATE INDEX idx_posts_featured ON posts (is_featured, featured_until);
CREATE INDEX idx_posts_published_at ON posts (published_at DESC);
CREATE INDEX idx_posts_status_created ON posts (status, created_at DESC);

-- 전문 검색 인덱스 (MySQL 5.6+)
ALTER TABLE posts ADD FULLTEXT INDEX idx_posts_search (title, content);

-- 외래키 제약조건 (members 테이블 존재 시)
-- ALTER TABLE posts ADD CONSTRAINT fk_posts_author 
-- FOREIGN KEY (author_id) REFERENCES members(id) ON DELETE CASCADE;

-- 게시글 통계를 위한 뷰 (선택사항)
CREATE VIEW post_stats AS
SELECT 
    COUNT(*) as total_posts,
    COUNT(CASE WHEN status = 'PUBLISHED' THEN 1 END) as published_posts,
    COUNT(CASE WHEN status = 'DRAFT' THEN 1 END) as draft_posts,
    COUNT(CASE WHEN status = 'HIDDEN' THEN 1 END) as hidden_posts,
    SUM(view_count) as total_views,
    AVG(view_count) as avg_views
FROM posts;

-- 샘플 데이터 삽입 (개발/테스트용)
INSERT INTO posts (title, content, author_id, status, view_count, is_featured, published_at) VALUES
('Spring Boot 게시판 개발하기', '
Spring Boot와 Kotlin을 사용하여 게시판을 개발하는 방법에 대해 알아보겠습니다.

## 1. 프로젝트 설정
- Spring Boot 2.7+
- Kotlin
- JPA/Hibernate
- MySQL

## 2. 엔티티 설계
Post 엔티티를 다음과 같이 설계합니다...
', 1, 'PUBLISHED', 25, TRUE, NOW()),

('JPA 연관관계 매핑', '
JPA에서 연관관계를 매핑하는 방법에 대해 설명합니다.

### @ManyToOne 관계
게시글과 작성자 간의 관계는 다대일 관계입니다.

### 지연 로딩 설정
성능을 위해 LAZY 로딩을 사용합니다.
', 1, 'PUBLISHED', 18, FALSE, NOW()),

('REST API 설계 원칙', '
RESTful API를 설계할 때 따라야 할 원칙들:

1. 자원(Resource) 기반 URL
2. HTTP 메서드 활용
3. 상태 코드 정확한 사용
4. 버전 관리
', 2, 'PUBLISHED', 42, TRUE, NOW()),

('작성 중인 글', '
아직 작성 중인 글입니다. 임시저장 상태입니다.

내용을 계속 작성해야 합니다...
', 2, 'DRAFT', 0, FALSE, NULL),

('관리자 공지사항', '
시스템 점검으로 인한 서비스 일시 중단 안내

점검 시간: 2024-01-15 02:00 ~ 06:00
점검 내용: 데이터베이스 업그레이드
', 1, 'HIDDEN', 100, FALSE, NOW());

-- 인기 게시글 조회 쿼리 예시
-- SELECT id, title, view_count, created_at 
-- FROM posts 
-- WHERE status = 'PUBLISHED' 
-- ORDER BY view_count DESC 
-- LIMIT 10;

-- 최신 게시글 조회 쿼리 예시  
-- SELECT p.id, p.title, p.created_at, m.company_name as author_name
-- FROM posts p
-- JOIN members m ON p.author_id = m.id
-- WHERE p.status = 'PUBLISHED'
-- ORDER BY p.created_at DESC
-- LIMIT 20;

-- 검색 쿼리 예시 (전문 검색)
-- SELECT id, title, MATCH(title, content) AGAINST('Spring Boot' IN NATURAL LANGUAGE MODE) as relevance
-- FROM posts 
-- WHERE status = 'PUBLISHED' 
-- AND MATCH(title, content) AGAINST('Spring Boot' IN NATURAL LANGUAGE MODE)
-- ORDER BY relevance DESC;