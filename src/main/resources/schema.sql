-- Add state_json column to room table if it doesn't exist
ALTER TABLE room ADD COLUMN IF NOT EXISTS state_json TEXT; 