-- Script to create the upcoming_meetings table if it doesn't exist
CREATE TABLE IF NOT EXISTS upcoming_meetings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  group_id BIGINT NOT NULL,
  meeting_date DATE NOT NULL,
  meeting_time VARCHAR(50) DEFAULT '9:00 AM - 11:00 AM',
  meeting_url VARCHAR(512),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_upcoming_meetings_group FOREIGN KEY (group_id) REFERENCES groupstudent (id) ON DELETE CASCADE
);

-- Add index for faster lookup
CREATE INDEX IF NOT EXISTS idx_upcoming_meetings_group_id ON upcoming_meetings(group_id);
CREATE INDEX IF NOT EXISTS idx_upcoming_meetings_date ON upcoming_meetings(meeting_date);

-- Message to confirm execution
SELECT 'Created or verified upcoming_meetings table' AS message; 