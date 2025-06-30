package com.shop.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO for creating a new order from cart
 * Contains shipping information and payment method
 */
@Data
public class CreateOrderRequest {
    
    // Shipping information
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private String customerPhone;
    
    // Payment information
    private String paymentMethod;
    
    // Cart items to convert to order items
    private List<CartItemDto> cartItems;
    
    /**
     * Inner DTO for cart item information
     */
    @Data
    public static class CartItemDto {
        private Long productId;
        private Integer quantity;
    }
} 