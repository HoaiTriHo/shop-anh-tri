package com.shop.backend.entity;

/**
 * Enum for order status tracking
 * Represents the different states an order can be in
 */
public enum OrderStatus {
    PENDING,    // Order placed but not yet processed
    CONFIRMED,  // Order confirmed by admin
    SHIPPED,    // Order shipped to customer
    DELIVERED,  // Order delivered to customer
    CANCELLED   // Order cancelled
} 