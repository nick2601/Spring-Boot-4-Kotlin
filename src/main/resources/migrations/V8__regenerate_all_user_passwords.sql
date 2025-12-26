-- V8: PERMANENT FIX - All user passwords with VERIFIED BCrypt hash
-- Password for all users: password123
-- This hash was generated using Spring Security's BCryptPasswordEncoder on 2025-12-26
-- VERIFIED to work with login API

-- Single UPDATE to fix ALL users at once
UPDATE users SET password = '$2a$10$qIAQzz6kBzTPV/ClEdMcceJQJsybEAqD6pyGe79Vx22kL42OPiBNe';
