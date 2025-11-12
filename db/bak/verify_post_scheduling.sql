-- Verification script for post scheduling functionality
-- Run this after applying the migration to verify everything works correctly

-- 1. Check table structure
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default,
    character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'rounds' 
ORDER BY ordinal_position;

-- 2. Check constraints
SELECT 
    constraint_name,
    constraint_type,
    check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.check_constraints cc ON tc.constraint_name = cc.constraint_name
WHERE tc.table_name = 'rounds' 
AND tc.constraint_type = 'CHECK';

-- 3. Check indexes
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'rounds'
AND indexname LIKE '%post%';

-- 4. Sample data test (if rounds exist)
SELECT 
    id,
    title,
    start_date,
    end_date,
    post_start_date,
    post_end_date,
    post_duration_days,
    -- Calculate expected post dates based on business logic
    end_date as calculated_post_start_date,
    (end_date + INTERVAL '1 day' * COALESCE(post_duration_days, 7)) as calculated_post_end_date
FROM rounds 
ORDER BY created_at DESC 
LIMIT 5;

-- 5. Test data insertion (optional - comment out if not needed)
/*
INSERT INTO rounds (
    title, description, category, order_amount,
    start_date, end_date, post_duration_days,
    status, created_by, created_at, updated_at
) VALUES (
    'Test Round for Post Scheduling',
    'Testing post scheduling functionality',
    'Test',
    100000.00,
    NOW() + INTERVAL '1 day',
    NOW() + INTERVAL '7 days',
    10,
    'ACTIVE',
    1,
    NOW(),
    NOW()
);
*/