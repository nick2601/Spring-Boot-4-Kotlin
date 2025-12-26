-- V11: Ensure email uniqueness in users table
-- This prevents duplicate email entries that cause login issues

-- First remove any duplicate emails (keep lowest ID)
DELETE u1 FROM users u1
INNER JOIN users u2
WHERE u1.id > u2.id AND u1.email = u2.email;

-- Add unique constraint to email column if not exists
-- Using ALTER IGNORE to skip if constraint already exists
ALTER TABLE users ADD UNIQUE INDEX IF NOT EXISTS idx_users_email_unique (email);

