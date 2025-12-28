-- V14: Add missing order columns to align entity and DB schema
-- Adds financial and address columns that the JPA entity expects

ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN IF NOT EXISTS tax DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN IF NOT EXISTS shipping_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN IF NOT EXISTS total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  ADD COLUMN IF NOT EXISTS shipping_address TEXT,
  ADD COLUMN IF NOT EXISTS billing_address TEXT,
  ADD COLUMN IF NOT EXISTS notes TEXT;

-- Indexes intentionally omitted to keep migration compatible with older MariaDB/MySQL versions
-- Add indexes separately if required for your environment
