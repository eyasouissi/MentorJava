-- Script to create the group_student_members table if it doesn't exist
CREATE TABLE IF NOT EXISTS group_student_members (
  group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  group_student_id BIGINT NOT NULL,
  PRIMARY KEY (group_id, user_id),
  CONSTRAINT fk_group_student_members_group FOREIGN KEY (group_id) REFERENCES groupstudent (id) ON DELETE CASCADE,
  CONSTRAINT fk_group_student_members_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
  CONSTRAINT fk_group_student_members_group_student FOREIGN KEY (group_student_id) REFERENCES groupstudent (id) ON DELETE CASCADE
);

-- Add index for faster lookup
CREATE INDEX IF NOT EXISTS idx_group_student_members_user_id ON group_student_members(user_id);
CREATE INDEX IF NOT EXISTS idx_group_student_members_group_id ON group_student_members(group_id);
CREATE INDEX IF NOT EXISTS idx_group_student_members_group_student_id ON group_student_members(group_student_id);

-- Message to confirm execution
SELECT 'Created or verified group_student_members table' AS message; 