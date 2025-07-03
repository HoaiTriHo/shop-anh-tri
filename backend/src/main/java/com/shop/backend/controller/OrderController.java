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
     * Get all orders for the current user
     * Accessible by USER role only
     * 
     * @return List of user's orders
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderDto>> getMyOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        List<OrderDto> orders = orderService.getOrdersByUsername(username);
        return ResponseEntity.ok(orders);
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
     * @return Paginated orders response
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "orderDate,desc") String sort) {
        
        Map<String, Object> response = orderService.getAllOrdersWithPagination(page, size, status, sort);
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
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        OrderDto updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
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
} 