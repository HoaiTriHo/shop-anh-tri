package com.shop.backend.controller;

import com.shop.backend.entity.Order;
import com.shop.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller cho dashboard admin.
 * Cung cấp các endpoint tổng hợp số liệu cho dashboard.
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin
public class DashboardController {
    private final DashboardService dashboardService;

    /**
     * Tổng quan: tổng đơn, doanh thu, khách, sản phẩm
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    /**
     * Doanh thu theo ngày (7 ngày gần nhất)
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getRevenueByDay() {
        return ResponseEntity.ok(dashboardService.getRevenueByDay());
    }

    /**
     * Số lượng đơn theo trạng thái
     */
    @GetMapping("/order-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getOrderStatusStats() {
        return ResponseEntity.ok(dashboardService.getOrderStatusStats());
    }

    /**
     * Top sản phẩm bán chạy
     */
    @GetMapping("/top-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getTopProducts() {
        return ResponseEntity.ok(dashboardService.getTopProducts());
    }

    /**
     * Đơn hàng mới nhất
     */
    @GetMapping("/recent-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getRecentOrders() {
        return ResponseEntity.ok(dashboardService.getRecentOrders());
    }
} 