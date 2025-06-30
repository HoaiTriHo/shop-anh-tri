package com.shop.backend.entity;

/**
 * Enum for payment status tracking
 * Represents the different states a payment can be in
 */
public enum PaymentStatus {
    PENDING,    // Payment not yet processed
    PAID,       // Payment completed
    FAILED,     // Payment failed
    REFUNDED    // Payment refunded
} 