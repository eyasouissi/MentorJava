-- Update created_by_id column type from INT to BIGINT if it exists
ALTER TABLE groupstudent 
MODIFY COLUMN created_by_id BIGINT;

-- Message to confirm execution
SELECT 'Updated created_by_id column type to BIGINT in groupstudent table' AS message; 