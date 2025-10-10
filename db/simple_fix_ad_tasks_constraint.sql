-- Simple fix for ad_tasks unique constraint
-- Run these commands step by step

-- Step 1: Check current constraints
SELECT constraint_name, constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'ad_tasks' AND constraint_type = 'UNIQUE';

-- Step 2: Drop the problematic constraint (replace 'constraint_name' with actual name from step 1)
-- Common constraint names to try:
ALTER TABLE ad_tasks DROP CONSTRAINT unique_round_member_type_index;
ALTER TABLE ad_tasks DROP CONSTRAINT uk_ad_tasks_unique;
ALTER TABLE ad_tasks DROP CONSTRAINT ad_tasks_unique_idx;

-- Step 3: Check for duplicate data before creating new constraint
SELECT round_id, member_id, ad_type, ad_index, COUNT(*) 
FROM ad_tasks 
GROUP BY round_id, member_id, ad_type, ad_index 
HAVING COUNT(*) > 1;

-- Step 4: If duplicates exist, remove them (keep the latest one)
DELETE FROM ad_tasks 
WHERE id NOT IN (
    SELECT id FROM (
        SELECT id, 
               ROW_NUMBER() OVER (
                   PARTITION BY round_id, member_id, ad_type, ad_index 
                   ORDER BY created_at DESC
               ) as rn
        FROM ad_tasks
    ) ranked 
    WHERE rn = 1
);

-- Step 5: Create the correct unique constraint
ALTER TABLE ad_tasks 
ADD CONSTRAINT uk_ad_tasks_round_member_type_index 
UNIQUE (round_id, member_id, ad_type, ad_index);

-- Step 6: Verify
SELECT constraint_name, constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'ad_tasks' AND constraint_type = 'UNIQUE';