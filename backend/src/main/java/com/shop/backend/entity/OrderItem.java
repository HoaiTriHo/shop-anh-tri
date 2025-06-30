package com.shop.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * OrderItem entity representing individual items in an order
 * This entity stores detailed information about each product in an order
 * 
 * Features:
 * - JPA entity mapping to database table
 * - Many-to-one relationship with Order and Product entities
 * - Stores quantity and price at time of order
 * - Calculates subtotal for each item
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-one relationship with Order entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Many-to-one relationship with Product entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * Constructor for creating a new order item
     * 
     * @param order The order this item belongs to
     * @param product The product being ordered
     * @param quantity The quantity of the product
     */
    public OrderItem(Order order, Product product, Integer quantity) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
        this.calculateSubtotal();
    }

    /**
     * Calculate the subtotal for this order item
     * This method multiplies quantity by unit price
     */
    public void calculateSubtotal() {
        this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    /**
     * Update quantity and recalculate subtotal
     * 
     * @param quantity The new quantity
     */
    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
        this.calculateSubtotal();
    }
} 