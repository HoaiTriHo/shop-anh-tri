package com.shop.backend.entity;

/**
 * Enum for order status tracking
 * Represents the different states an order can be in
 */
public enum OrderStatus {
    PENDING("Chờ xử lý"),           // Order placed but not yet processed
    CONFIRMED("Đã xác nhận"),       // Order confirmed by admin
    PROCESSING("Đang xử lý"),       // Order is being processed
    SHIPPING("Đang giao hàng"),     // Order is being shipped
    DELIVERED("Đã giao hàng"),      // Order delivered to customer
    CANCELLED("Đã hủy");           // Order cancelled
    
    private final String displayName;
    
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 