package com.shop.backend.repository;

import com.shop.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for OrderItem entity
 * Provides database operations for order items
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find all order items by order ID
     * 
     * @param orderId The ID of the order
     * @return List of order items for the specified order
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * Find all order items by product ID
     * Useful for analyzing product sales
     * 
     * @param productId The ID of the product
     * @return List of order items containing the specified product
     */
    List<OrderItem> findByProductId(Long productId);
    
    /**
     * Delete all order items by order ID
     * Used when cancelling an order
     * 
     * @param orderId The ID of the order
     */
    void deleteByOrderId(Long orderId);
} 