-- Update orders table schema for checkout functionality
-- This script adds new columns to support shipping info, status, and payment

-- Drop existing order_products table if exists (old ManyToMany relationship)
DROP TABLE IF EXISTS order_products;

-- Add new columns to orders table (chỉ thêm nếu chưa tồn tại)
SET @col := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'orders' AND COLUMN_NAME = 'customer_name');
SET @sql := IF(@col = 0, 'ALTER TABLE orders ADD COLUMN customer_name VARCHAR(255) NOT NULL DEFAULT "";', 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'orders' AND COLUMN_NAME = 'customer_email');
SET @sql := IF(@col = 0, 'ALTER TABLE orders ADD COLUMN customer_email VARCHAR(255) NOT NULL DEFAULT "";', 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'orders' AND COLUMN_NAME = 'shipping_address');
SET @sql := IF(@col = 0, 'ALTER TABLE orders ADD COLUMN shipping_address TEXT NOT NULL;', 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'orders' AND COLUMN_NAME = 'customer_phone');
SET @sql := IF(@col = 0, 'ALTER TABLE orders ADD COLUMN customer_phone VARCHAR(20) NOT NULL DEFAULT "";', 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'orders' AND COLUMN_NAME = 'status');
SET @sql := IF(@col = 0, 'ALTER TABLE orders ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT "PENDING";', 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'orders' AND COLUMN_NAME = 'payment_method');
SET @sql := IF(@col = 0, 'ALTER TABLE orders ADD COLUMN payment_method VARCHAR(50);', 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'orders' AND COLUMN_NAME = 'payment_status');
SET @sql := IF(@col = 0, 'ALTER TABLE orders ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT "PENDING";', 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Create order_items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Chỉ tạo index cho các cột mới (status, payment_status)
SET @idx := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_orders_status');
SET @sql := IF(@idx = 0, 'CREATE INDEX idx_orders_status ON orders(status);', 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = 'orders' AND INDEX_NAME = 'idx_orders_payment_status');
SET @sql := IF(@idx = 0, 'CREATE INDEX idx_orders_payment_status ON orders(payment_status);', 'SELECT 1;');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Update existing orders to have default values
UPDATE orders SET 
    customer_name = 'Default Customer',
    customer_email = 'default@example.com',
    shipping_address = 'Default Address',
    customer_phone = '0000000000',
    status = 'PENDING',
    payment_status = 'PENDING'
WHERE customer_name = '';

-- Insert sample order items for existing orders (if any)
-- This is just for demonstration, you may want to remove this in production
INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal)
SELECT 
    o.id,
    p.id,
    1,
    p.price,
    p.price
FROM orders o
CROSS JOIN products p
LIMIT 5; 