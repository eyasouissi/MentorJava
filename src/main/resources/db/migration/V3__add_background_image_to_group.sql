-- Add background_image column to groupstudent table
ALTER TABLE groupstudent ADD COLUMN IF NOT EXISTS background_image VARCHAR(255); 