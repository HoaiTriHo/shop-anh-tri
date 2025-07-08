-- Add more products with real images
-- This script adds additional products to make the shop more diverse

USE shop;

-- Add more electronics
INSERT INTO products (name, description, price, stock_quantity, category, brand, image_url) VALUES
('iPad Pro 12.9"', 'Professional tablet with M2 chip and Liquid Retina XDR display', 900000, 25, 'Electronics', 'Apple', 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400&h=400&fit=crop&crop=center'),
('AirPods Pro', 'Active noise cancellation wireless earbuds', 200000, 60, 'Electronics', 'Apple', 'https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?w=400&h=400&fit=crop&crop=center'),
('Google Pixel 8', 'Android phone with advanced AI camera features', 350000, 35, 'Electronics', 'Google', 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=400&h=400&fit=crop&crop=center'),
('Microsoft Surface Laptop', 'Premium Windows laptop with touch screen', 950000, 20, 'Electronics', 'Microsoft', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=400&fit=crop&crop=center');

-- Add more clothing
INSERT INTO products (name, description, price, stock_quantity, category, brand, image_url) VALUES
('Nike Dri-FIT Shirt', 'Moisture-wicking athletic shirt for workouts', 120000, 120, 'Clothing', 'Nike', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=400&fit=crop&crop=center'),
('Adidas Track Jacket', 'Classic athletic jacket with three stripes', 180000, 80, 'Clothing', 'Adidas', 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400&h=400&fit=crop&crop=center'),
('H&M Hoodie', 'Comfortable cotton hoodie in various colors', 150000, 150, 'Clothing', 'H&M', 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=400&h=400&fit=crop&crop=center'),
('Zara Blazer', 'Professional blazer for office wear', 250000, 45, 'Clothing', 'Zara', 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=400&h=400&fit=crop&crop=center');

-- Add more sports equipment
INSERT INTO products (name, description, price, stock_quantity, category, brand, image_url) VALUES
('Basketball', 'Official size basketball for indoor/outdoor use', 100000, 75, 'Sports', 'Spalding', 'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=400&h=400&fit=crop&crop=center'),
('Tennis Racket', 'Professional tennis racket with carrying case', 200000, 30, 'Sports', 'Wilson', 'https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400&h=400&fit=crop&crop=center'),
('Dumbbells Set', 'Adjustable dumbbells set 5-50 lbs', 350000, 25, 'Sports', 'Bowflex', 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&h=400&fit=crop&crop=center'),
('Resistance Bands', 'Set of 5 resistance bands for home workouts', 110000, 100, 'Sports', 'Generic', 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&h=400&fit=crop&crop=center');

-- Add home & lifestyle products
INSERT INTO products (name, description, price, stock_quantity, category, brand, image_url) VALUES
('Coffee Maker', 'Programmable coffee maker with thermal carafe', 180000, 40, 'Home', 'Cuisinart', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400&h=400&fit=crop&crop=center'),
('Bluetooth Speaker', 'Portable waterproof bluetooth speaker', 150000, 55, 'Electronics', 'JBL', 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400&h=400&fit=crop&crop=center'),
('Smart Watch', 'Fitness tracking smartwatch with heart rate monitor', 300000, 35, 'Electronics', 'Fitbit', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=400&fit=crop&crop=center'),
('Backpack', 'Durable laptop backpack with multiple compartments', 120000, 90, 'Lifestyle', 'Jansport', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400&h=400&fit=crop&crop=center');

-- Verify all products
SELECT COUNT(*) as total_products FROM products;
SELECT category, COUNT(*) as count FROM products GROUP BY category ORDER BY count DESC;
