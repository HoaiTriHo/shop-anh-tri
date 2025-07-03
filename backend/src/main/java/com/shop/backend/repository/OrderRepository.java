package com.shop.backend.repository;

import com.shop.backend.entity.Order;
import com.shop.backend.entity.User;
import com.shop.backend.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity
 * This interface provides data access methods for order operations
 * 
 * Features:
 * - Extends JpaRepository for basic CRUD operations
 * - Custom query methods for order management and analytics
 * - Pagination support for order history
 * - Spring Data JPA automatic query generation
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by user
     * This method is used to display user's order history
     * 
     * @param user The user whose orders to find
     * @return List of orders for the specified user
     */
    List<Order> findByUser(User user);

    /**
     * Find orders by user with pagination
     * This method is used for paginated order history display
     * 
     * @param user The user whose orders to find
     * @param pageable Pagination information
     * @return Page of orders for the specified user
     */
    Page<Order> findByUser(User user, Pageable pageable);

    /**
     * Find orders by date range
     * This method is used for order analytics and reporting
     * 
     * @param startDate Start date for the range
     * @param endDate End date for the range
     * @return List of orders within the date range
     */
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders by total price range
     * This method is used for order value analysis
     * 
     * @param minPrice Minimum order price
     * @param maxPrice Maximum order price
     * @return List of orders within the price range
     */
    List<Order> findByTotalPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find orders by user and date range
     * This method is used for user-specific order history filtering
     * 
     * @param user The user whose orders to find
     * @param startDate Start date for the range
     * @param endDate End date for the range
     * @return List of orders for the user within the date range
     */
    List<Order> findByUserAndOrderDateBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count orders by user
     * This method is used for user analytics
     * 
     * @param user The user whose orders to count
     * @return Number of orders for the specified user
     */
    long countByUser(User user);

    /**
     * Find recent orders
     * This method is used for displaying recent order activity
     * 
     * @param limit Maximum number of orders to return
     * @return List of recent orders
     */
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders(@Param("limit") int limit);

    /**
     * Find orders with high value
     * This method is used for identifying high-value orders
     * 
     * @param threshold Minimum order value threshold
     * @return List of orders above the threshold
     */
    @Query("SELECT o FROM Order o WHERE o.totalPrice >= :threshold ORDER BY o.totalPrice DESC")
    List<Order> findHighValueOrders(@Param("threshold") BigDecimal threshold);

    /**
     * Calculate total revenue for a date range
     * This method is used for revenue analytics
     * 
     * @param startDate Start date for the range
     * @param endDate End date for the range
     * @return Total revenue for the date range
     */
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find orders with minimum number of products
     * This method is used for order analysis
     * 
     * @param minProducts Minimum number of products
     * @return List of orders with at least the specified number of products
     */
    @Query("SELECT o FROM Order o WHERE SIZE(o.orderItems) >= :minProducts")
    List<Order> findOrdersWithMinimumProducts(@Param("minProducts") int minProducts);

    /**
     * Find average order value
     * This method is used for analytics
     * 
     * @return Average order value
     */
    @Query("SELECT AVG(o.totalPrice) FROM Order o")
    BigDecimal findAverageOrderValue();

    /**
     * Find order by ID and user (for security - user can only see their own orders)
     * 
     * @param id Order ID
     * @param user User entity
     * @return Optional containing the order if found and belongs to the user
     */
    Optional<Order> findByIdAndUser(Long id, User user);

    /**
     * Find orders by status
     * This method is used for filtering orders by their status
     * 
     * @param status Order status to filter by
     * @return List of orders with the specified status
     */
    List<Order> findByStatus(com.shop.backend.entity.OrderStatus status);

    /**
     * Lấy 5 đơn hàng mới nhất
     */
    List<Order> findTop5ByOrderByOrderDateDesc();

    /**
     * Đếm số lượng đơn theo trạng thái
     */
    long countByStatus(OrderStatus status);
} 