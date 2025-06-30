-- Update orders table schema for checkout functionality
-- This script adds new columns to support shipping info, status, and payment

-- Drop existing order_products table if exists (old ManyToMany relationship)
DROP TABLE IF EXISTS order_products;

-- Add new columns to orders table
ALTER TABLE orders 
ADD COLUMN customer_name VARCHAR(255) NOT NULL DEFAULT '',
ADD COLUMN customer_email VARCHAR(255) NOT NULL DEFAULT '',
ADD COLUMN shipping_address TEXT NOT NULL,
ADD COLUMN customer_phone VARCHAR(20) NOT NULL DEFAULT '',
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
ADD COLUMN payment_method VARCHAR(50),
ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Create order_items table
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);

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