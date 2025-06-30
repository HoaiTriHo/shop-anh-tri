#!/bin/bash

echo "🔍 KIỂM TRA SẴN SÀNG CHO DOCKER COMPOSE"
echo "========================================"

# Kiểm tra Docker
echo "1. Kiểm tra Docker..."
if docker --version > /dev/null 2>&1; then
    echo "   ✅ Docker đã cài đặt: $(docker --version)"
else
    echo "   ❌ Docker chưa cài đặt hoặc không hoạt động"
    exit 1
fi

# Kiểm tra Docker Compose
echo "2. Kiểm tra Docker Compose..."
if docker-compose --version > /dev/null 2>&1; then
    echo "   ✅ Docker Compose đã cài đặt: $(docker-compose --version)"
else
    echo "   ❌ Docker Compose chưa cài đặt"
    exit 1
fi

# Kiểm tra file docker-compose.yml
echo "3. Kiểm tra docker-compose.yml..."
if [ -f "docker-compose.yml" ]; then
    echo "   ✅ docker-compose.yml tồn tại"
else
    echo "   ❌ docker-compose.yml không tồn tại"
    exit 1
fi

# Kiểm tra backend Dockerfile
echo "4. Kiểm tra backend Dockerfile..."
if [ -f "backend/Dockerfile" ]; then
    echo "   ✅ backend/Dockerfile tồn tại"
else
    echo "   ❌ backend/Dockerfile không tồn tại"
    exit 1
fi

# Kiểm tra frontend Dockerfile
echo "5. Kiểm tra frontend Dockerfile..."
if [ -f "frontend/Dockerfile" ]; then
    echo "   ✅ frontend/Dockerfile tồn tại"
else
    echo "   ❌ frontend/Dockerfile không tồn tại"
    exit 1
fi

# Kiểm tra nginx.conf
echo "6. Kiểm tra nginx.conf..."
if [ -f "frontend/nginx.conf" ]; then
    echo "   ✅ frontend/nginx.conf tồn tại"
else
    echo "   ❌ frontend/nginx.conf không tồn tại"
    exit 1
fi

# Kiểm tra init.sql
echo "7. Kiểm tra init.sql..."
if [ -f "mysql/init.sql" ]; then
    echo "   ✅ mysql/init.sql tồn tại"
else
    echo "   ❌ mysql/init.sql không tồn tại"
    exit 1
fi

# Kiểm tra backend build
echo "8. Kiểm tra backend build..."
if [ -f "backend/build.gradle" ]; then
    echo "   ✅ backend/build.gradle tồn tại"
else
    echo "   ❌ backend/build.gradle không tồn tại"
    exit 1
fi

# Kiểm tra frontend package.json
echo "9. Kiểm tra frontend package.json..."
if [ -f "frontend/package.json" ]; then
    echo "   ✅ frontend/package.json tồn tại"
else
    echo "   ❌ frontend/package.json không tồn tại"
    exit 1
fi

# Kiểm tra cấu hình ports
echo "10. Kiểm tra cấu hình ports..."
if grep -q "3000:3000" docker-compose.yml && grep -q "8080:8080" docker-compose.yml && grep -q "3306:3306" docker-compose.yml; then
    echo "   ✅ Ports được cấu hình đúng: 3000, 8080, 3306"
else
    echo "   ❌ Ports chưa được cấu hình đúng"
    exit 1
fi

# Kiểm tra database name
echo "11. Kiểm tra database name..."
if grep -q "MYSQL_DATABASE: shop" docker-compose.yml; then
    echo "   ✅ Database name: shop"
else
    echo "   ❌ Database name chưa đúng"
    exit 1
fi

# Kiểm tra container name
echo "12. Kiểm tra container name..."
if grep -q "container_name: shop-db" docker-compose.yml; then
    echo "   ✅ Container name: shop-db"
else
    echo "   ❌ Container name chưa đúng"
    exit 1
fi

echo ""
echo "🎉 TẤT CẢ ĐÃ SẴN SÀNG!"
echo "======================"
echo "Bạn có thể chạy:"
echo "  docker-compose up --build"
echo ""
echo "Sau đó truy cập:"
echo "  Frontend: http://localhost:3000"
echo "  Backend API: http://localhost:8080/api"
echo "  Database: localhost:3306"
echo ""
echo "Để dừng: docker-compose down" 