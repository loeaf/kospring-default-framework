-- Add description column to ad_tasks table
-- 광고 작업에 상세 설명 필드 추가

ALTER TABLE ad_tasks
    ADD COLUMN description VARCHAR(20000) NULL COMMENT '광고 작업 상세 설명';