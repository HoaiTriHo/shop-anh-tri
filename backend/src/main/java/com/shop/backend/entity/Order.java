package com.shop.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity representing a customer order in the e-commerce system
 * This entity manages order information with OneToMany relationship to OrderItems
 * 
 * Features:
 * - JPA entity mapping to database table
 * - One-to-many relationship with OrderItem entities
 * - Many-to-one relationship with User entity
 * - Shipping information storage
 * - Order status tracking
 * - Total price calculation
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-one relationship with User entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // One-to-many relationship with OrderItem entities
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    // Shipping information
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    // Order status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    // Payment information
    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    /**
     * Constructor for creating a new order
     * 
     * @param user The user who placed the order
     */
    public Order(User user) {
        this.user = user;
        this.orderDate = LocalDateTime.now();
        this.orderItems = new ArrayList<>();
        this.totalPrice = BigDecimal.ZERO;
        this.status = OrderStatus.PENDING;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    /**
     * Add an order item to the order
     * This method adds the item and recalculates the total price
     * 
     * @param orderItem The order item to add
     */
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
        calculateTotalPrice();
    }

    /**
     * Remove an order item from the order
     * This method removes the item and recalculates the total price
     * 
     * @param orderItem The order item to remove
     */
    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
        calculateTotalPrice();
    }

    /**
     * Calculate the total price of the order
     * This method sums up all order item subtotals
     */
    public void calculateTotalPrice() {
        this.totalPrice = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get the number of items in the order
     * 
     * @return Total number of items
     */
    public int getItemCount() {
        return orderItems.size();
    }

    /**
     * Get the total quantity of all items in the order
     * 
     * @return Total quantity
     */
    public int getTotalQuantity() {
        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }
} 