# Mentor Application

## Recent Updates

### Group Module Updates
- Added max_members field to GroupStudent entity
- Created SQL migration script to add max_members column to groupstudent table
- Changed createdById field type from int to Long to match User.id type

### Database Migration Required
To update your database schema, execute the following SQL:

```sql
-- Add max_members column if it doesn't exist
ALTER TABLE groupstudent 
ADD COLUMN IF NOT EXISTS max_members INT DEFAULT 10;

-- Update created_by_id column type from INT to BIGINT if it exists
ALTER TABLE groupstudent 
MODIFY COLUMN created_by_id BIGINT;
```

This can be done through your database management tool or by running:
```bash
mysql -u [username] -p [database_name] < src/main/resources/sql/add_max_members_to_groupstudent.sql
mysql -u [username] -p [database_name] < src/main/resources/sql/update_created_by_id_column.sql
```

### Fixed Issues
- Fixed "Cannot resolve method 'setMaxMembers' in 'GroupStudent'" by adding the missing field and methods
- Fixed "'setCreatedById(int)' in 'GroupStudent' cannot be applied to '(java.lang.Long)'" by changing field type to Long
- Added proper max members handling in group creation 