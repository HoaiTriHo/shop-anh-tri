-- MySQL initialization script for E-commerce Shop Database
-- This script sets up the database and inserts sample data

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS shop;
USE shop;

-- Set character set and collation
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    address TEXT,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    is_account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    is_credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL,
    image_url VARCHAR(200),
    category VARCHAR(50) NOT NULL,
    brand VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_brand (brand),
    INDEX idx_active (is_active),
    INDEX idx_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create orders table (simplified)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_order_date (order_date),
    INDEX idx_total_price (total_price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create order_products junction table for ManyToMany relationship
CREATE TABLE IF NOT EXISTS order_products (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample users
INSERT INTO users (username, email, password, first_name, last_name, role, created_at) VALUES
('admin', 'admin@shop.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Admin', 'User', 'ADMIN', NOW()),
('john_doe', 'john@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'John', 'Doe', 'USER', NOW()),
('jane_smith', 'jane@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Jane', 'Smith', 'USER', NOW());

-- Insert sample products
INSERT INTO products (name, description, price, stock_quantity, category, brand, image_url) VALUES
('iPhone 15 Pro', 'Latest iPhone with advanced camera system and A17 Pro chip', 999.99, 50, 'Electronics', 'Apple', 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=400&h=400&fit=crop&crop=center'),
('Samsung Galaxy S24', 'Premium Android smartphone with AI features', 899.99, 45, 'Electronics', 'Samsung', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop&crop=center'),
('MacBook Pro 14"', 'Professional laptop with M3 chip for developers', 1999.99, 30, 'Electronics', 'Apple', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=400&fit=crop&crop=center'),
('Nike Air Max 270', 'Comfortable running shoes with Air Max technology', 129.99, 100, 'Sports', 'Nike', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=400&fit=crop&crop=center'),
('Adidas Ultraboost 22', 'Premium running shoes with Boost technology', 179.99, 75, 'Sports', 'Adidas', 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=400&h=400&fit=crop&crop=center'),
('Levi\'s 501 Jeans', 'Classic straight fit jeans in blue denim', 59.99, 200, 'Clothing', 'Levi\'s', 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=400&h=400&fit=crop&crop=center'),
('Uniqlo T-Shirt', 'Comfortable cotton t-shirt in various colors', 19.99, 300, 'Clothing', 'Uniqlo', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=400&fit=crop&crop=center'),
('Sony WH-1000XM5', 'Premium noise-cancelling headphones', 349.99, 25, 'Electronics', 'Sony', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop&crop=center'),
('Dell XPS 13', 'Ultrabook with InfinityEdge display', 1299.99, 20, 'Electronics', 'Dell', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=400&fit=crop&crop=center'),
('Yoga Mat', 'Non-slip yoga mat for home workouts', 29.99, 150, 'Sports', 'Generic', 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&h=400&fit=crop&crop=center');

-- Insert sample orders
INSERT INTO orders (user_id, total_price, order_date) VALUES
(2, 999.99, NOW() - INTERVAL 5 DAY),
(3, 179.99, NOW() - INTERVAL 3 DAY),
(2, 1299.99, NOW() - INTERVAL 1 DAY);

-- Insert sample order_products relationships
INSERT INTO order_products (order_id, product_id) VALUES
(1, 1),  -- Order 1: iPhone 15 Pro
(2, 5),  -- Order 2: Adidas Ultraboost 22
(3, 9);  -- Order 3: Dell XPS 13

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Create indexes for better performance
CREATE INDEX idx_products_price_range ON products(price) WHERE is_active = TRUE;
CREATE INDEX idx_orders_user_total ON orders(user_id, total_price);
CREATE INDEX idx_order_products_order ON order_products(order_id);

-- Grant permissions to root user
GRANT ALL PRIVILEGES ON shop.* TO 'root'@'%';
FLUSH PRIVILEGES; 