package com.shop.backend.service;

import com.shop.backend.entity.Order;
import com.shop.backend.entity.OrderItem;
import com.shop.backend.entity.OrderStatus;
import com.shop.backend.entity.Product;
import com.shop.backend.entity.User;
import com.shop.backend.repository.OrderItemRepository;
import com.shop.backend.repository.OrderRepository;
import com.shop.backend.repository.ProductRepository;
import com.shop.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service tổng hợp số liệu cho dashboard admin.
 * Cung cấp các hàm lấy số liệu tổng quan, doanh thu, trạng thái đơn, top sản phẩm, đơn mới nhất.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Lấy số liệu tổng quan: tổng đơn, doanh thu, khách hàng, sản phẩm
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", orderRepository.count());
        // Chỉ tính doanh thu các đơn đã thanh toán hoặc đã giao
        BigDecimal revenue = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.CONFIRMED)
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalRevenue", revenue);
        result.put("totalCustomers", userRepository.count());
        result.put("totalProducts", productRepository.count());
        return result;
    }

    /**
     * Thống kê doanh thu theo ngày (7 ngày gần nhất)
     */
    public List<Map<String, Object>> getRevenueByDay() {
        List<Order> orders = orderRepository.findAll();
        LocalDate today = LocalDate.now();
        // Lấy 7 ngày gần nhất
        List<LocalDate> days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            days.add(today.minusDays(i));
        }
        // Group doanh thu theo ngày
        List<Map<String, Object>> result = new ArrayList<>();
        for (LocalDate day : days) {
            BigDecimal sum = orders.stream()
                    .filter(o -> (o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.CONFIRMED)
                            && o.getOrderDate().toLocalDate().equals(day))
                    .map(Order::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", day.format(DateTimeFormatter.ISO_DATE));
            entry.put("revenue", sum);
            result.add(entry);
        }
        return result;
    }

    /**
     * Thống kê số lượng đơn theo trạng thái
     */
    public Map<String, Long> getOrderStatusStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            stats.put(status.name(), orderRepository.countByStatus(status));
        }
        return stats;
    }

    /**
     * Top 5 sản phẩm bán chạy nhất (theo số lượng)
     */
    public List<Map<String, Object>> getTopProducts() {
        // Lấy tất cả order items, group by product, sum quantity
        List<OrderItem> allOrderItems = orderItemRepository.findAll();
        
        // Group by product and sum quantities
        Map<Product, Long> productQuantities = new HashMap<>();
        for (OrderItem item : allOrderItems) {
            Product product = item.getProduct();
            productQuantities.merge(product, (long) item.getQuantity(), Long::sum);
        }
        
        // Sort by quantity descending and take top 5
        List<Map<String, Object>> result = productQuantities.entrySet().stream()
                .sorted(Map.Entry.<Product, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> productInfo = new HashMap<>();
                    productInfo.put("id", entry.getKey().getId());
                    productInfo.put("name", entry.getKey().getName());
                    productInfo.put("imageUrl", entry.getKey().getImageUrl());
                    productInfo.put("quantity", entry.getValue());
                    return productInfo;
                })
                .collect(Collectors.toList());
        
        return result;
    }

    /**
     * Lấy 5 đơn hàng mới nhất có tên khách hàng
     * Chỉ lấy những đơn hàng có customerName không rỗng
     */
    public List<Map<String, Object>> getRecentOrders() {
        // Lấy tất cả đơn hàng, sắp xếp theo ngày mới nhất
        List<Order> allOrders = orderRepository.findRecentOrders(100); // Lấy 100 đơn gần nhất để filter
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Order order : allOrders) {
            // Chỉ xử lý đơn hàng có tên khách hàng hợp lệ
            String customerName = order.getCustomerName();
            System.out.println("DEBUG: Order ID " + order.getId() + " has customerName: '" + customerName + "'");
            
            // Kiểm tra: không null, không rỗng, không chỉ có khoảng trắng, không phải chuỗi rỗng ""
            boolean isValidCustomerName = customerName != null && 
                !customerName.isEmpty() && 
                !customerName.trim().isEmpty() && 
                !customerName.equals("");
            
            System.out.println("DEBUG: Is valid customerName? " + isValidCustomerName);
            
            if (isValidCustomerName) {
                Map<String, Object> orderInfo = new HashMap<>();
                orderInfo.put("id", order.getId());
                orderInfo.put("customerName", customerName);
                orderInfo.put("customerEmail", order.getCustomerEmail());
                orderInfo.put("orderDate", order.getOrderDate());
                orderInfo.put("status", order.getStatus().name());
                orderInfo.put("totalPrice", order.getTotalPrice());
                result.add(orderInfo);
                
                System.out.println("DEBUG: Added order ID " + order.getId() + " to result");
                
                // Chỉ lấy tối đa 5 đơn hàng
                if (result.size() >= 5) {
                    break;
                }
            } else {
                System.out.println("DEBUG: Skipped order ID " + order.getId() + " due to invalid customerName");
            }
        }
        return result;
    }
} 