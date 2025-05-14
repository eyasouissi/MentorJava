-- Add max_members column if it doesn't exist
ALTER TABLE groupstudent 
ADD COLUMN IF NOT EXISTS max_members INT DEFAULT 10;

-- Message to confirm execution
SELECT 'Added max_members column to groupstudent table' AS message; 