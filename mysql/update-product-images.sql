-- Update product images with real URLs
-- This script updates the image_url field with real product images

USE shop;

-- Update iPhone 15 Pro
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=400&h=400&fit=crop&crop=center'
WHERE name = 'iPhone 15 Pro';

-- Update Samsung Galaxy S24
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop&crop=center'
WHERE name = 'Samsung Galaxy S24';

-- Update MacBook Pro 14"
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=400&fit=crop&crop=center'
WHERE name = 'MacBook Pro 14"';

-- Update Nike Air Max 270
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=400&fit=crop&crop=center'
WHERE name = 'Nike Air Max 270';

-- Update Adidas Ultraboost 22
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=400&h=400&fit=crop&crop=center'
WHERE name = 'Adidas Ultraboost 22';

-- Update Levi\'s 501 Jeans
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=400&h=400&fit=crop&crop=center'
WHERE name = 'Levi\'s 501 Jeans';

-- Update Uniqlo T-Shirt
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=400&fit=crop&crop=center'
WHERE name = 'Uniqlo T-Shirt';

-- Update Sony WH-1000XM5
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop&crop=center'
WHERE name = 'Sony WH-1000XM5';

-- Update Dell XPS 13
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=400&fit=crop&crop=center'
WHERE name = 'Dell XPS 13';

-- Update Yoga Mat
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&h=400&fit=crop&crop=center'
WHERE name = 'Yoga Mat';

-- Verify the updates
SELECT id, name, image_url FROM products ORDER BY id;
