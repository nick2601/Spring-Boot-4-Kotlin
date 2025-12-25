-- V7: Fix BCrypt password hashes for Spring Security compatibility
-- Convert $2b$ prefix (Python/Node.js) to $2a$ prefix (Java/Spring Security)
-- Both are functionally identical, just different version identifiers
-- Password for all users: password123

-- Convert all $2b$ prefixed passwords to $2a$ prefix
UPDATE users SET password = REPLACE(password, '$2b$', '$2a$') WHERE password LIKE '$2b$%';

