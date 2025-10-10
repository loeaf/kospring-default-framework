-- 게시판 테이블 생성 DDL

-- 게시글 테이블
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시글 ID',
    title VARCHAR(200) NOT NULL COMMENT '제목',
    content TEXT COMMENT '내용',
    author_id BIGINT NOT NULL COMMENT '작성자 ID',
    status ENUM('DRAFT', 'PUBLISHED', 'HIDDEN') NOT NULL DEFAULT 'DRAFT' COMMENT '게시글 상태',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT '조회수',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE COMMENT '추천 여부',
    featured_until DATETIME NULL COMMENT '추천 종료일시',
    published_at DATETIME NULL COMMENT '발행일시',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    -- 외래키 제약조건 (members 테이블이 존재한다고 가정)
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES members(id) ON DELETE CASCADE,
    
    -- 인덱스
    INDEX idx_posts_status (status),
    INDEX idx_posts_author_id (author_id),
    INDEX idx_posts_created_at (created_at),
    INDEX idx_posts_view_count (view_count),
    INDEX idx_posts_featured (is_featured, featured_until),
    INDEX idx_posts_published_at (published_at),
    INDEX idx_posts_status_created (status, created_at),
    
    -- 전문 검색 인덱스 (MySQL의 경우)
    FULLTEXT INDEX idx_posts_search (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 테이블';

-- 샘플 데이터 삽입 (테스트용)
INSERT INTO posts (title, content, author_id, status, view_count, is_featured, published_at) VALUES
('첫 번째 게시글', '이것은 첫 번째 게시글의 내용입니다.', 1, 'PUBLISHED', 10, TRUE, NOW()),
('두 번째 게시글', '이것은 두 번째 게시글의 내용입니다.', 1, 'PUBLISHED', 5, FALSE, NOW()),
('임시 저장된 글', '아직 작성 중인 글입니다.', 2, 'DRAFT', 0, FALSE, NULL),
('숨겨진 글', '관리자에 의해 숨겨진 글입니다.', 2, 'HIDDEN', 15, FALSE, NOW());