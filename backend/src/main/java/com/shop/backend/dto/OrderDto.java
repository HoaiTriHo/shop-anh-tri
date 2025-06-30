package com.shop.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for order information
 * Contains complete order details for API responses
 */
@Data
public class OrderDto {
    
    private Long id;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private String customerPhone;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private List<OrderItemDto> orderItems;
    
    /**
     * Inner DTO for order item information
     */
    @Data
    public static class OrderItemDto {
        private Long id;
        private Long productId;
        private String productName;
        private String productImageUrl;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
} 