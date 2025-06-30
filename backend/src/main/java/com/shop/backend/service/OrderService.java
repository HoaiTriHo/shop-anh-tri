package com.shop.backend.service;

import com.shop.backend.dto.CreateOrderRequest;
import com.shop.backend.dto.OrderDto;
import com.shop.backend.entity.*;
import com.shop.backend.repository.OrderItemRepository;
import com.shop.backend.repository.OrderRepository;
import com.shop.backend.repository.ProductRepository;
import com.shop.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Order business logic
 * This class handles all order-related operations including checkout
 * 
 * Features:
 * - Order creation from cart
 * - Order management and status updates
 * - Business logic validation
 * - Transaction management
 * - DTO conversion
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Create a new order from cart items
     * This method converts cart items to order items and creates the order
     * 
     * @param request Order creation request with shipping info and cart items
     * @param username Username of the order creator
     * @return Created order DTO
     */
    public OrderDto createOrderFromCart(CreateOrderRequest request, String username) {
        // Find the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate cart items
        if (request.getCartItems() == null || request.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Create new order
        Order order = new Order(user);
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setShippingAddress(request.getShippingAddress());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setPaymentMethod(request.getPaymentMethod());

        // Process each cart item
        for (CreateOrderRequest.CartItemDto cartItemDto : request.getCartItems()) {
            Product product = productRepository.findById(cartItemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItemDto.getProductId()));

            // Validate stock
            if (product.getStockQuantity() < cartItemDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Create order item
            OrderItem orderItem = new OrderItem(order, product, cartItemDto.getQuantity());
            order.addOrderItem(orderItem);

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - cartItemDto.getQuantity());
            productRepository.save(product);
        }

        // Save order (this will also save order items due to cascade)
        Order savedOrder = orderRepository.save(order);
        
        return convertToDto(savedOrder);
    }

    /**
     * Get all orders for a specific user by username
     * 
     * @param username Username
     * @return List of order DTOs
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByUser(user)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get order by ID and username (user can only see their own orders)
     * 
     * @param orderId Order ID
     * @param username Username
     * @return Order DTO
     */
    @Transactional(readOnly = true)
    public OrderDto getOrderByIdAndUsername(Long orderId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return convertToDto(order);
    }

    /**
     * Get all orders (admin only)
     * 
     * @return List of all order DTOs
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by user ID (admin only)
     * 
     * @param userId User ID
     * @return List of order DTOs for the user
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByUser(user)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update order status (admin only)
     * 
     * @param orderId Order ID
     * @param status New status
     * @return Updated order DTO
     */
    public OrderDto updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    /**
     * Update payment status
     * 
     * @param orderId Order ID
     * @param paymentStatus New payment status
     * @return Updated order DTO
     */
    public OrderDto updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setPaymentStatus(paymentStatus);
        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    /**
     * Delete an order
     * 
     * @param orderId Order ID
     * @return true if order was deleted, false if not found
     */
    public boolean deleteOrder(Long orderId) {
        if (orderRepository.existsById(orderId)) {
            orderRepository.deleteById(orderId);
            return true;
        }
        return false;
    }

    /**
     * Convert Order entity to OrderDto
     * 
     * @param order Order entity
     * @return OrderDto
     */
    private OrderDto convertToDto(Order order) {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(order.getId());
        orderDto.setUserId(order.getUser().getId());
        orderDto.setCustomerName(order.getCustomerName());
        orderDto.setCustomerEmail(order.getCustomerEmail());
        orderDto.setShippingAddress(order.getShippingAddress());
        orderDto.setCustomerPhone(order.getCustomerPhone());
        orderDto.setTotalPrice(order.getTotalPrice());
        orderDto.setOrderDate(order.getOrderDate());
        orderDto.setStatus(order.getStatus().name());
        orderDto.setPaymentMethod(order.getPaymentMethod());
        orderDto.setPaymentStatus(order.getPaymentStatus().name());

        // Convert order items
        List<OrderDto.OrderItemDto> orderItemDtos = order.getOrderItems()
                .stream()
                .map(this::convertOrderItemToDto)
                .collect(Collectors.toList());
        orderDto.setOrderItems(orderItemDtos);

        return orderDto;
    }

    /**
     * Convert OrderItem entity to OrderItemDto
     * 
     * @param orderItem OrderItem entity
     * @return OrderItemDto
     */
    private OrderDto.OrderItemDto convertOrderItemToDto(OrderItem orderItem) {
        OrderDto.OrderItemDto dto = new OrderDto.OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setProductImageUrl(orderItem.getProduct().getImageUrl());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setSubtotal(orderItem.getSubtotal());
        return dto;
    }
} 