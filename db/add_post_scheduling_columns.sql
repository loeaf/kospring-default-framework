-- Add post scheduling columns to rounds table
-- This migration adds columns for managing post start/end dates and duration

ALTER TABLE rounds 
ADD COLUMN post_start_date TIMESTAMP,
ADD COLUMN post_end_date TIMESTAMP,
ADD COLUMN post_duration_days INTEGER DEFAULT 7;

-- Add comments for documentation
COMMENT ON COLUMN rounds.post_start_date IS '게시 시작일 (명시적으로 설정된 경우)';
COMMENT ON COLUMN rounds.post_end_date IS '게시 종료일 (명시적으로 설정된 경우)';
COMMENT ON COLUMN rounds.post_duration_days IS '게시 기간 (일수, 기본값: 7일)';

-- Add constraints
ALTER TABLE rounds 
ADD CONSTRAINT chk_post_duration_days 
CHECK (post_duration_days >= 1 AND post_duration_days <= 90);

-- Add index for performance on date queries
CREATE INDEX idx_rounds_post_dates ON rounds(post_start_date, post_end_date);

-- Update existing rounds to have default post_duration_days if NULL
UPDATE rounds 
SET post_duration_days = 7 
WHERE post_duration_days IS NULL;
