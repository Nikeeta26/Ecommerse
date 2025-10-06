-- Make order_type column nullable
ALTER TABLE orders ALTER COLUMN order_type DROP NOT NULL;
