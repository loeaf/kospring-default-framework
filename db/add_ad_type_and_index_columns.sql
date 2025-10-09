-- Add ad_type and ad_index columns to ad_tasks table
-- Fix unique constraint to allow multiple ads per member per round

-- First, add the missing columns
ALTER TABLE ad_tasks 
ADD COLUMN IF NOT EXISTS ad_type VARCHAR(20),
ADD COLUMN IF NOT EXISTS ad_index INTEGER;

-- Drop the old unique constraint that prevents multiple ads per member per round
ALTER TABLE ad_tasks DROP CONSTRAINT IF EXISTS unique_round_member;

-- Add new unique constraint that allows multiple ads with different types
ALTER TABLE ad_tasks ADD CONSTRAINT unique_round_member_type_index 
UNIQUE (round_id, member_id, ad_type, ad_index);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_ad_tasks_ad_type ON ad_tasks(ad_type);
CREATE INDEX IF NOT EXISTS idx_ad_tasks_ad_index ON ad_tasks(ad_index);
CREATE INDEX IF NOT EXISTS idx_ad_tasks_type_index ON ad_tasks(ad_type, ad_index);

-- Add comments for the new columns
COMMENT ON COLUMN ad_tasks.ad_type IS '광고 유형 (scratch, carousel, interactive)';
COMMENT ON COLUMN ad_tasks.ad_index IS '광고 순서 번호 (1, 2, 3)';

-- Update existing records to have default values
UPDATE ad_tasks 
SET ad_type = 'scratch', ad_index = 1 
WHERE ad_type IS NULL;