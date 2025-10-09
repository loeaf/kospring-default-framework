-- 광고 생성 작업을 관리하는 테이블 생성

CREATE TABLE IF NOT EXISTS ad_tasks (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    task_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ad_content TEXT,
    html_file_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    
    -- 인덱스
    CONSTRAINT unique_round_member UNIQUE (round_id, member_id),
    CONSTRAINT check_task_status CHECK (task_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'RETRY')),
    CONSTRAINT check_retry_count CHECK (retry_count >= 0 AND retry_count <= 10)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_ad_tasks_round_id ON ad_tasks(round_id);
CREATE INDEX IF NOT EXISTS idx_ad_tasks_member_id ON ad_tasks(member_id);
CREATE INDEX IF NOT EXISTS idx_ad_tasks_status ON ad_tasks(task_status);
CREATE INDEX IF NOT EXISTS idx_ad_tasks_created_at ON ad_tasks(created_at);

-- 테이블에 대한 코멘트 추가
COMMENT ON TABLE ad_tasks IS '라운드별 회원 광고 생성 작업 관리 테이블';
COMMENT ON COLUMN ad_tasks.id IS '작업 고유 ID';
COMMENT ON COLUMN ad_tasks.round_id IS '라운드 ID';
COMMENT ON COLUMN ad_tasks.member_id IS '회원 ID';
COMMENT ON COLUMN ad_tasks.task_status IS '작업 상태 (PENDING, PROCESSING, COMPLETED, FAILED, RETRY)';
COMMENT ON COLUMN ad_tasks.ad_content IS 'AI가 생성한 광고 콘텐츠';
COMMENT ON COLUMN ad_tasks.html_file_path IS '생성된 HTML 파일 경로';
COMMENT ON COLUMN ad_tasks.created_at IS '작업 생성 시간';
COMMENT ON COLUMN ad_tasks.updated_at IS '작업 수정 시간';
COMMENT ON COLUMN ad_tasks.started_at IS '작업 시작 시간';
COMMENT ON COLUMN ad_tasks.completed_at IS '작업 완료 시간';
COMMENT ON COLUMN ad_tasks.error_message IS '오류 메시지';
COMMENT ON COLUMN ad_tasks.retry_count IS '재시도 횟수';