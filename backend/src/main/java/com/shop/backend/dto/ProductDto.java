package com.shop.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Product entity
 * This class is used for API requests and responses
 * 
 * Features:
 * - Simplified data structure for API communication
 * - Input validation annotations
 * - Lombok annotations for reducing boilerplate code
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String category;
    private String brand;
    private Integer stockQuantity;
    private boolean active;
} 