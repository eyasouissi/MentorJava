-- Add meeting_url column if it doesn't exist
ALTER TABLE groupstudent 
ADD COLUMN IF NOT EXISTS meeting_url VARCHAR(512) DEFAULT NULL;

-- Message to confirm execution
SELECT 'Added meeting_url column to groupstudent table' AS message; 