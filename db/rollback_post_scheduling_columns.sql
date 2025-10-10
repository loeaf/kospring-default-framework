-- Rollback script for post scheduling columns
-- Use this script to remove the post scheduling columns if needed

-- Drop the index first
DROP INDEX IF EXISTS idx_rounds_post_dates;

-- Drop the constraint
ALTER TABLE rounds DROP CONSTRAINT IF EXISTS chk_post_duration_days;

-- Drop the columns
ALTER TABLE rounds 
DROP COLUMN IF EXISTS post_start_date,
DROP COLUMN IF EXISTS post_end_date,
DROP COLUMN IF EXISTS post_duration_days;

-- Verify the rollback
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'rounds' 
AND column_name IN ('post_start_date', 'post_end_date', 'post_duration_days')
ORDER BY ordinal_position;