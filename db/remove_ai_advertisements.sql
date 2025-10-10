-- Remove ai_advertisements table and related dependencies
-- This migration replaces ai_advertisements functionality with ad_tasks

-- 1. Drop triggers and functions that depend on ai_advertisements
DROP TRIGGER IF EXISTS trigger_ad_task_sync ON ad_tasks;
DROP FUNCTION IF EXISTS trigger_sync_ad_task_to_ai_advertisement();
DROP FUNCTION IF EXISTS sync_ad_task_to_ai_advertisement(BIGINT);
DROP FUNCTION IF EXISTS sync_all_ad_tasks_to_ai_advertisements();

-- 2. Drop the advertisement_overview view
DROP VIEW IF EXISTS advertisement_overview;

-- 3. Drop the create_advertisement_assignments function (it uses ai_advertisements)
DROP FUNCTION IF EXISTS create_advertisement_assignments(BIGINT);

-- 4. Update advertisement_assignments table to reference ad_tasks directly
-- First, drop the foreign key constraint to ai_advertisements
ALTER TABLE advertisement_assignments 
DROP CONSTRAINT IF EXISTS advertisement_assignments_ai_advertisement_id_fkey;

-- Rename the column to ad_task_id
ALTER TABLE advertisement_assignments 
RENAME COLUMN ai_advertisement_id TO ad_task_id;

-- Add foreign key constraint to ad_tasks
ALTER TABLE advertisement_assignments 
ADD CONSTRAINT advertisement_assignments_ad_task_id_fkey 
FOREIGN KEY (ad_task_id) REFERENCES ad_tasks(id) ON DELETE CASCADE;

-- 5. Update existing data to map ai_advertisement_id to corresponding ad_task_id
-- This assumes that each ai_advertisement has a corresponding ad_task with same round_id and member_id
UPDATE advertisement_assignments aa
SET ad_task_id = (
    SELECT at.id 
    FROM ad_tasks at 
    WHERE at.round_id = aa.round_id 
    AND at.member_id = aa.advertiser_member_id
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1 
    FROM ad_tasks at 
    WHERE at.round_id = aa.round_id 
    AND at.member_id = aa.advertiser_member_id
);

-- 6. Drop the ai_advertisements table
DROP TABLE IF EXISTS ai_advertisements CASCADE;

-- 7. Create new simplified advertisement_overview view using only ad_tasks
CREATE OR REPLACE VIEW advertisement_overview AS
SELECT 
    -- ad_tasks 정보
    at.id as ad_task_id,
    at.round_id,
    at.member_id,
    at.task_status,
    at.ad_content as ad_html_content,
    at.html_file_path as ad_html_file_path,
    at.web_url as ad_web_url,
    at.ad_type,
    at.ad_index,
    at.created_at as ad_task_created_at,
    at.completed_at as ad_task_completed_at,
    
    -- 라운드 정보
    r.title as round_title,
    r.category as round_category,
    r.order_amount,
    r.start_date as round_start_date,
    r.end_date as round_end_date,
    r.status as round_status,
    
    -- 회원 정보
    m.company_name,
    m.email as member_email
    
FROM ad_tasks at
LEFT JOIN rounds r ON at.round_id = r.id
LEFT JOIN members m ON at.member_id = m.id;

-- 8. Create new create_advertisement_assignments function using ad_tasks
CREATE OR REPLACE FUNCTION create_advertisement_assignments(p_round_id BIGINT)
RETURNS VOID AS $$
DECLARE
    participant_count INTEGER;
    revenue_per_post DECIMAL(12,2);
    round_order_amount DECIMAL(12,2);
BEGIN
    -- 라운드 참여자 수와 발주 금액 조회
    SELECT COUNT(*), r.order_amount
    INTO participant_count, round_order_amount
    FROM orders o
    JOIN ad_tasks at ON o.ad_task_id = at.id
    JOIN rounds r ON at.round_id = r.id
    WHERE at.round_id = p_round_id AND o.status = 'COMPLETED'
    GROUP BY r.order_amount;

    -- 게시당 수익 계산 (발주금액 ÷ (참여자수-1))
    revenue_per_post := round_order_amount / (participant_count - 1);

    -- 모든 참여자에 대해 광고 할당 생성 (ad_tasks 기반)
    INSERT INTO advertisement_assignments (
        round_id, advertiser_member_id, publisher_member_id,
        ad_task_id, revenue_per_post
    )
    SELECT
        p_round_id,
        advertiser_ad_tasks.member_id as advertiser_member_id,
        publisher_ad_tasks.member_id as publisher_member_id,
        advertiser_ad_tasks.id as ad_task_id,
        revenue_per_post
    FROM ad_tasks advertiser_ad_tasks
    JOIN orders advertiser_orders ON advertiser_orders.ad_task_id = advertiser_ad_tasks.id
    CROSS JOIN ad_tasks publisher_ad_tasks
    JOIN orders publisher_orders ON publisher_orders.ad_task_id = publisher_ad_tasks.id
    WHERE advertiser_ad_tasks.round_id = p_round_id
        AND publisher_ad_tasks.round_id = p_round_id
        AND advertiser_ad_tasks.member_id != publisher_ad_tasks.member_id  -- 자기 광고는 게시 안함
        AND advertiser_orders.status = 'COMPLETED'
        AND publisher_orders.status = 'COMPLETED'
        AND advertiser_ad_tasks.task_status = 'COMPLETED';  -- 완료된 광고만 할당
END;
$$ LANGUAGE plpgsql;