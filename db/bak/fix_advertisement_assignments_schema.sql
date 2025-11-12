-- Fix advertisement_assignments table schema
-- Change ai_advertisement_id column to ad_task_id to match entity mapping

-- 1. Drop existing foreign key constraint
ALTER TABLE advertisement_assignments 
DROP CONSTRAINT IF EXISTS advertisement_assignments_ai_advertisement_id_fkey;

-- 2. Rename column from ai_advertisement_id to ad_task_id
ALTER TABLE advertisement_assignments 
RENAME COLUMN ai_advertisement_id TO ad_task_id;

-- 3. Add new foreign key constraint referencing ad_tasks table
ALTER TABLE advertisement_assignments 
ADD CONSTRAINT advertisement_assignments_ad_task_id_fkey 
FOREIGN KEY (ad_task_id) REFERENCES ad_tasks(id) ON DELETE CASCADE;

-- 4. Update existing data to map to corresponding ad_tasks
-- This maps existing ai_advertisement_id values to ad_task_id values
UPDATE advertisement_assignments 
SET ad_task_id = (
    SELECT at.id 
    FROM ad_tasks at 
    INNER JOIN ai_advertisements ai ON ai.round_id = at.round_id AND ai.advertiser_member_id = at.member_id
    WHERE ai.id = advertisement_assignments.ad_task_id
    LIMIT 1
)
WHERE ad_task_id IN (SELECT id FROM ai_advertisements);