#!/bin/bash

echo "🎨 Testing Dark Theme Dashboard APIs"
echo "====================================="

# Login để lấy token
echo "🔐 Logging in as admin..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"global","password":"123456"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ Login failed!"
    exit 1
fi

echo "✅ Login successful! Token: ${TOKEN:0:20}..."

# Test dashboard summary
echo ""
echo "📊 Testing Dashboard Summary..."
SUMMARY_RESPONSE=$(curl -s -X GET http://localhost:8080/api/admin/dashboard/summary \
  -H "Authorization: Bearer $TOKEN")

echo "Summary Response: $SUMMARY_RESPONSE"

# Test revenue chart
echo ""
echo "📈 Testing Revenue Chart..."
REVENUE_RESPONSE=$(curl -s -X GET http://localhost:8080/api/admin/dashboard/revenue \
  -H "Authorization: Bearer $TOKEN")

echo "Revenue Response: $REVENUE_RESPONSE"

# Test order status
echo ""
echo "📋 Testing Order Status..."
STATUS_RESPONSE=$(curl -s -X GET http://localhost:8080/api/admin/dashboard/order-status \
  -H "Authorization: Bearer $TOKEN")

echo "Status Response: $STATUS_RESPONSE"

# Test top products
echo ""
echo "🏆 Testing Top Products..."
PRODUCTS_RESPONSE=$(curl -s -X GET http://localhost:8080/api/admin/dashboard/top-products \
  -H "Authorization: Bearer $TOKEN")

echo "Top Products Response: $PRODUCTS_RESPONSE"

# Test recent orders
echo ""
echo "🛒 Testing Recent Orders..."
ORDERS_RESPONSE=$(curl -s -X GET http://localhost:8080/api/admin/dashboard/recent-orders \
  -H "Authorization: Bearer $TOKEN")

echo "Recent Orders Response: $ORDERS_RESPONSE"

echo ""
echo "🎉 Dark Theme Dashboard Test Complete!"
echo ""
echo "🌐 Frontend URL: http://localhost:3000"
echo "🔧 Backend URL: http://localhost:8080"
echo ""
echo "💡 To test the dark theme UI:"
echo "   1. Open http://localhost:3000"
echo "   2. Login with username: global, password: 123456"
echo "   3. Navigate to Admin Dashboard"
echo "   4. Enjoy the new dark theme! 🎨" 