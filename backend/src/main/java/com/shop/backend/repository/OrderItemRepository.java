package com.shop.backend.repository;

import com.shop.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

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

    /**
     * Truy vấn top sản phẩm bán chạy nhất (theo số lượng)
     * Trả về: Object[]{Product, Long quantity}
     */
    @Query("SELECT oi.product, SUM(oi.quantity) as totalQty FROM OrderItem oi GROUP BY oi.product ORDER BY totalQty DESC")
    List<Object[]> findTopProductsByQuantity(Pageable pageable);

    // Tiện ích: lấy top N sản phẩm bán chạy
    default List<Object[]> findTopProductsByQuantity(int limit) {
        return findTopProductsByQuantity(org.springframework.data.domain.PageRequest.of(0, limit));
    }
} 