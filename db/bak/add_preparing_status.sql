-- Add PREPARING status to rounds table status constraint
-- This migration adds PREPARING status to allow rounds to be created in preparation state

-- Drop the existing check constraint
ALTER TABLE rounds DROP CONSTRAINT rounds_status_check;

-- Add new check constraint with PREPARING status
ALTER TABLE rounds ADD CONSTRAINT rounds_status_check 
    CHECK (status IN ('PREPARING', 'ACTIVE', 'CLOSED', 'PENDING'));

-- Update default status to PREPARING
ALTER TABLE rounds ALTER COLUMN status SET DEFAULT 'PREPARING';

-- Update existing ACTIVE rounds to PREPARING (optional - if you want to test)
-- UPDATE rounds SET status = 'PREPARING' WHERE status = 'ACTIVE';

-- Verify the constraint
SELECT conname, consrc 
FROM pg_constraint 
WHERE conname = 'rounds_status_check';