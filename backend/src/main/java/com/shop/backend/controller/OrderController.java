package com.shop.backend.controller;

import com.shop.backend.dto.CreateOrderRequest;
import com.shop.backend.dto.OrderDto;
import com.shop.backend.entity.OrderStatus;
import com.shop.backend.entity.PaymentStatus;
import com.shop.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Order operations
 * This controller provides order management functionality including checkout
 * 
 * Features:
 * - Order creation from cart (USER only)
 * - Order listing by user (USER, ADMIN)
 * - Order details (USER, ADMIN)
 * - Order status management (ADMIN only)
 * - Role-based access control
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin
public class OrderController {

    private final OrderService orderService;

    /**
     * Create a new order from cart (checkout)
     * Accessible by USER role only
     * 
     * @param request Order creation request with shipping info and cart items
     * @return Created order
     */
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> checkout(@Valid @RequestBody CreateOrderRequest request) {
        // Get current user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        OrderDto createdOrder = orderService.createOrderFromCart(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * Get all orders for the current user with filtering, sorting and pagination
     * Accessible by USER role only
     * 
     * @param status Order status filter (optional: PENDING, CONFIRMED, PROCESSING, SHIPPING, DELIVERED, CANCELLED)
     * @param startDate Start date filter (optional: yyyy-MM-dd)
     * @param endDate End date filter (optional: yyyy-MM-dd)
     * @param sort Sort option (optional: orderDate,desc or orderDate,asc)
     * @param page Page number (0-based, default: 0)
     * @param size Page size (default: 10)
     * @return Paginated orders response with metadata
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getMyOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "orderDate,desc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Map<String, Object> response = orderService.getOrdersByUsernameWithFilterAndPagination(username, status, startDate, endDate, sort, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Get order by ID (user can only see their own orders)
     * Accessible by USER role only
     * 
     * @param id Order ID
     * @return Order details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        OrderDto order = orderService.getOrderByIdAndUsername(id, username);
        return ResponseEntity.ok(order);
    }

    /**
     * Get all orders with pagination, status filter, and sorting (admin only)
     * Accessible by ADMIN role only
     * 
     * @param page Page number (0-based, default: 0)
     * @param size Page size (default: 10)
     * @param status Order status filter (optional)
     * @param sort Sort option (e.g. orderDate,desc)
     * @param keyword Keyword filter (optional)
     * @return Paginated orders response
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "orderDate,desc") String sort,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> response = orderService.getAllOrdersWithPagination(page, size, status, sort, keyword);
        return ResponseEntity.ok(response);
    }

    /**
     * Get orders by user ID (admin only)
     * Accessible by ADMIN role only
     * 
     * @param userId User ID
     * @return List of orders for the specified user
     */
    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderDto> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Update order status (admin only)
     * Accessible by ADMIN role only
     * 
     * @param orderId Order ID
     * @param status New order status
     * @return Updated order
     */
    @PutMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        try {
            OrderDto updatedOrder = orderService.updateOrderStatus(orderId, status);
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Cập nhật trạng thái đơn hàng thành công",
                "order", updatedOrder
            );
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Không thể cập nhật trạng thái đơn hàng: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update payment status (admin only)
     * Accessible by ADMIN role only
     * 
     * @param orderId Order ID
     * @param paymentStatus New payment status
     * @return Updated order
     */
    @PutMapping("/admin/{orderId}/payment-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam PaymentStatus paymentStatus) {
        OrderDto updatedOrder = orderService.updatePaymentStatus(orderId, paymentStatus);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * Delete order (admin only)
     * Accessible by ADMIN role only
     * 
     * @param orderId Order ID
     * @return Success response
     */
    @DeleteMapping("/admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        boolean deleted = orderService.deleteOrder(orderId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cancel order by user (only if status is PENDING)
     * Accessible by USER role only
     * User can only cancel their own orders
     * 
     * @param orderId Order ID
     * @return Success response or error message
     */
    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        try {
            OrderDto cancelledOrder = orderService.cancelOrderByUser(orderId, username);
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Đơn hàng đã được hủy thành công",
                "order", cancelledOrder
            );
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Không thể hủy đơn hàng: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
} 