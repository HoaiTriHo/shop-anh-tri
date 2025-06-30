package com.shop.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity representing a product in the e-commerce system
 * This entity stores all product information including price, description, and inventory
 * 
 * Features:
 * - JPA entity mapping to database table
 * - Input validation using Bean Validation
 * - Automatic timestamp management for creation and updates
 * - Lombok annotations for reducing boilerplate code
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Positive(message = "Stock quantity must be positive")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Size(max = 200, message = "Image URL cannot exceed 200 characters")
    @Column(name = "image_url")
    private String imageUrl;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    @Column(nullable = false)
    private String category;

    @Size(max = 50, message = "Brand cannot exceed 50 characters")
    private String brand;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Timestamp fields for tracking creation and updates
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new product with basic information
     * This constructor is used when adding new products to the system
     * 
     * @param name Product name
     * @param description Product description
     * @param price Product price
     * @param stockQuantity Available stock quantity
     * @param category Product category
     * @param brand Product brand
     */
    public Product(String name, String description, BigDecimal price, Integer stockQuantity, String category, String brand) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.brand = brand;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Pre-persist method to set creation timestamp
     * This method is automatically called before the entity is persisted
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Pre-update method to set update timestamp
     * This method is automatically called before the entity is updated
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if product is in stock
     * 
     * @return true if stock quantity is greater than 0
     */
    public boolean isInStock() {
        return stockQuantity > 0;
    }

    /**
     * Decrease stock quantity by specified amount
     * This method is used when products are purchased
     * 
     * @param quantity Amount to decrease stock by
     * @return true if stock was successfully decreased
     */
    public boolean decreaseStock(int quantity) {
        if (stockQuantity >= quantity) {
            stockQuantity -= quantity;
            return true;
        }
        return false;
    }

    /**
     * Increase stock quantity by specified amount
     * This method is used when restocking products
     * 
     * @param quantity Amount to increase stock by
     */
    public void increaseStock(int quantity) {
        stockQuantity += quantity;
    }
} 