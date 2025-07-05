package com.shop.backend.controller;

import com.shop.backend.entity.Order;
import com.shop.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
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

    /**
     * Export báo cáo doanh thu ra file Excel (admin)
     * @param fromDate ngày bắt đầu (yyyy-MM-dd, optional)
     * @param toDate ngày kết thúc (yyyy-MM-dd, optional)
     * @return file Excel
     */
    @GetMapping("/export-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportReportExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        byte[] excel = dashboardService.exportReportExcel(fromDate, toDate);
        String filename = "shop-report-" + (fromDate != null ? fromDate : "all") + "-" + (toDate != null ? toDate : "now") + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
} 