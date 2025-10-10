-- STEP BY STEP CONSTRAINT FIX FOR AD_TASKS TABLE
-- Execute each step separately and check results

-- ============================================
-- STEP 1: INSPECT CURRENT STATE
-- ============================================
\echo '=== STEP 1: Current Constraints ==='
SELECT 
    tc.constraint_name,
    tc.constraint_type,
    string_agg(kcu.column_name, ', ' ORDER BY kcu.ordinal_position) as columns
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name = 'ad_tasks' 
    AND tc.constraint_type = 'UNIQUE'
GROUP BY tc.constraint_name, tc.constraint_type;

-- ============================================
-- STEP 2: CHECK FOR DUPLICATE DATA
-- ============================================
\echo '=== STEP 2: Check for Duplicates ==='
SELECT 
    round_id, 
    member_id, 
    ad_type, 
    ad_index, 
    COUNT(*) as count,
    string_agg(id::text, ', ') as ids
FROM ad_tasks 
GROUP BY round_id, member_id, ad_type, ad_index
HAVING COUNT(*) > 1
ORDER BY count DESC;

-- ============================================
-- STEP 3: BACKUP EXISTING DATA (OPTIONAL)
-- ============================================
\echo '=== STEP 3: Create Backup (Optional) ==='
-- CREATE TABLE ad_tasks_backup AS SELECT * FROM ad_tasks;

-- ============================================
-- STEP 4: REMOVE DUPLICATES (IF ANY)
-- ============================================
\echo '=== STEP 4: Remove Duplicates ==='
-- Keep the most recent record for each (round_id, member_id, ad_type, ad_index) combination
WITH duplicates_to_delete AS (
    SELECT id
    FROM (
        SELECT 
            id,
            ROW_NUMBER() OVER (
                PARTITION BY round_id, member_id, ad_type, ad_index 
                ORDER BY created_at DESC, id DESC
            ) as rn
        FROM ad_tasks
    ) ranked 
    WHERE rn > 1
)
DELETE FROM ad_tasks 
WHERE id IN (SELECT id FROM duplicates_to_delete);

-- Show how many duplicates were removed
SELECT COUNT(*) as duplicates_removed FROM duplicates_to_delete;

-- ============================================
-- STEP 5: DROP OLD CONSTRAINT
-- ============================================
\echo '=== STEP 5: Drop Old Constraint ==='
-- Try different possible constraint names
DO $$ 
DECLARE
    constraint_rec RECORD;
BEGIN
    FOR constraint_rec IN 
        SELECT constraint_name 
        FROM information_schema.table_constraints 
        WHERE table_name = 'ad_tasks' 
        AND constraint_type = 'UNIQUE'
        AND constraint_name != 'ad_tasks_pkey'  -- Don't drop primary key
    LOOP
        EXECUTE 'ALTER TABLE ad_tasks DROP CONSTRAINT ' || constraint_rec.constraint_name;
        RAISE NOTICE 'Dropped constraint: %', constraint_rec.constraint_name;
    END LOOP;
END $$;

-- ============================================
-- STEP 6: CREATE NEW CONSTRAINT
-- ============================================
\echo '=== STEP 6: Create New Constraint ==='
ALTER TABLE ad_tasks 
ADD CONSTRAINT uk_ad_tasks_round_member_type_index 
UNIQUE (round_id, member_id, ad_type, ad_index);

-- ============================================
-- STEP 7: VERIFY RESULT
-- ============================================
\echo '=== STEP 7: Verify New Constraint ==='
SELECT 
    tc.constraint_name,
    tc.constraint_type,
    string_agg(kcu.column_name, ', ' ORDER BY kcu.ordinal_position) as columns
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name = 'ad_tasks' 
    AND tc.constraint_type = 'UNIQUE'
GROUP BY tc.constraint_name, tc.constraint_type;

-- ============================================
-- STEP 8: TEST THE FIX
-- ============================================
\echo '=== STEP 8: Test - Check No More Duplicates ==='
SELECT 
    round_id, 
    member_id, 
    ad_type, 
    ad_index, 
    COUNT(*) as count
FROM ad_tasks 
GROUP BY round_id, member_id, ad_type, ad_index
HAVING COUNT(*) > 1;

\echo 'If no rows returned above, constraint fix was successful!';