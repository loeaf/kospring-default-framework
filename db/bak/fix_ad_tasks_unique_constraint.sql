-- Fix unique constraint for ad_tasks table
-- Current constraint seems to be preventing proper duplicate checking

-- 1. First, check existing constraint
SELECT 
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    tc.constraint_type
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name = 'ad_tasks' 
    AND tc.constraint_type = 'UNIQUE'
ORDER BY tc.constraint_name, kcu.ordinal_position;

-- 2. Drop existing unique constraint (if it exists)
-- Note: Replace 'unique_round_member_type_index' with actual constraint name from step 1
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE table_name = 'ad_tasks' 
        AND constraint_name = 'unique_round_member_type_index'
    ) THEN
        ALTER TABLE ad_tasks DROP CONSTRAINT unique_round_member_type_index;
        RAISE NOTICE 'Dropped existing constraint: unique_round_member_type_index';
    END IF;
END $$;

-- Alternative constraint names to check and drop if they exist
DO $$ 
BEGIN
    -- Check common constraint name patterns
    IF EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE table_name = 'ad_tasks' 
        AND constraint_name LIKE '%unique%'
        AND constraint_type = 'UNIQUE'
    ) THEN
        -- Get the constraint name and drop it
        DECLARE
            constraint_name_var text;
        BEGIN
            SELECT constraint_name INTO constraint_name_var
            FROM information_schema.table_constraints 
            WHERE table_name = 'ad_tasks' 
            AND constraint_name LIKE '%unique%'
            AND constraint_type = 'UNIQUE'
            LIMIT 1;
            
            IF constraint_name_var IS NOT NULL THEN
                EXECUTE 'ALTER TABLE ad_tasks DROP CONSTRAINT ' || constraint_name_var;
                RAISE NOTICE 'Dropped constraint: %', constraint_name_var;
            END IF;
        END;
    END IF;
END $$;

-- 3. Create the correct unique constraint
-- This should match the business logic: one ad task per (round, member, ad_type, ad_index)
ALTER TABLE ad_tasks 
ADD CONSTRAINT uk_ad_tasks_round_member_type_index 
UNIQUE (round_id, member_id, ad_type, ad_index);

-- 4. Create index for better performance
CREATE INDEX IF NOT EXISTS idx_ad_tasks_round_member 
ON ad_tasks (round_id, member_id);

-- 5. Verify the new constraint
SELECT 
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    tc.constraint_type
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name = 'ad_tasks' 
    AND tc.constraint_type = 'UNIQUE'
ORDER BY tc.constraint_name, kcu.ordinal_position;

-- 6. Check for any duplicate data that might prevent constraint creation
SELECT 
    round_id, 
    member_id, 
    ad_type, 
    ad_index, 
    COUNT(*) as duplicate_count
FROM ad_tasks 
GROUP BY round_id, member_id, ad_type, ad_index
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC, round_id, member_id;

COMMENT ON CONSTRAINT uk_ad_tasks_round_member_type_index ON ad_tasks 
IS 'Ensures unique ad task per round, member, ad type, and ad index combination';