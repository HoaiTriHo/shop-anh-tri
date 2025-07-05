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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;

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
        
        // Kiểm tra nếu đơn hàng đã bị khách hàng hủy thì không cho phép thay đổi
        if (order.getStatus() == OrderStatus.CUSTOMER_CANCELLED) {
            throw new IllegalStateException("Không thể thay đổi trạng thái đơn hàng đã bị khách hàng hủy");
        }
        
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
     * Cancel order by user (only if status is PENDING)
     * This method allows users to cancel their own orders
     * Only orders with PENDING status can be cancelled
     * When cancelled, product stock is restored
     * 
     * @param orderId Order ID
     * @param username Username of the order owner
     * @return Cancelled order DTO
     * @throws IllegalStateException if order cannot be cancelled
     */
    public OrderDto cancelOrderByUser(Long orderId, String username) {
        // Find the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the order and verify ownership
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Check if order can be cancelled (only PENDING status)
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Đơn hàng đã xác nhận, không thể hủy");
        }

        // Restore product stock
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
            productRepository.save(product);
        }

        // Update order status to CANCELLED
        order.setStatus(OrderStatus.CUSTOMER_CANCELLED);
        Order savedOrder = orderRepository.save(order);
        
        return convertToDto(savedOrder);
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
        orderDto.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "UNKNOWN");

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

    /**
     * Get all orders with pagination, status filter, and sorting (admin only)
     * 
     * @param page Page number (0-based)
     * @param size Page size
     * @param status Order status filter (optional)
     * @param sort Sort option (e.g. orderDate,desc)
     * @param keyword Keyword for search (optional)
     * @return Map containing orders, pagination info
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAllOrdersWithPagination(int page, int size, String status, String sort, String keyword) {
        List<Order> allOrders;
        // Nếu có keyword thì search theo keyword
        if (keyword != null && !keyword.trim().isEmpty()) {
            allOrders = orderRepository.searchOrdersByKeyword(keyword.trim());
        } else if (status != null && !status.isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                allOrders = orderRepository.findByStatus(orderStatus);
            } catch (IllegalArgumentException e) {
                allOrders = new ArrayList<>();
            }
        } else {
            allOrders = orderRepository.findAll();
        }
        
        // Sort
        String sortField = "orderDate";
        boolean desc = true;
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split(",");
            sortField = parts[0];
            if (parts.length > 1) {
                desc = parts[1].equalsIgnoreCase("desc");
            }
        }
        final String finalSortField = sortField;
        final boolean finalDesc = desc;
        allOrders.sort((o1, o2) -> {
            int cmp = 0;
            switch (finalSortField) {
                case "orderDate":
                    cmp = o1.getOrderDate().compareTo(o2.getOrderDate());
                    break;
                case "customerName":
                    cmp = o1.getCustomerName().compareToIgnoreCase(o2.getCustomerName());
                    break;
                case "status":
                    cmp = o1.getStatus().name().compareTo(o2.getStatus().name());
                    break;
                case "totalPrice":
                    cmp = o1.getTotalPrice().compareTo(o2.getTotalPrice());
                    break;
                case "id":
                    cmp = o1.getId().compareTo(o2.getId());
                    break;
                default:
                    cmp = o1.getOrderDate().compareTo(o2.getOrderDate());
            }
            return finalDesc ? -cmp : cmp;
        });
        
        // Calculate pagination
        int totalElements = allOrders.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // Apply pagination
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        List<Order> paginatedOrders = allOrders.subList(startIndex, endIndex);
        
        // Convert to DTOs
        List<OrderDto> orderDtos = paginatedOrders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("content", orderDtos);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("currentPage", page);
        response.put("pageSize", size);
        response.put("hasNext", page < totalPages - 1);
        response.put("hasPrevious", page > 0);
        
        return response;
    }

    /**
     * Get all orders for a specific user by username with filtering and sorting
     * 
     * @param username Username
     * @param status Order status filter (optional)
     * @param startDate Start date filter (optional: yyyy-MM-dd)
     * @param endDate End date filter (optional: yyyy-MM-dd)
     * @param sort Sort option (orderDate,desc or orderDate,asc)
     * @return List of order DTOs with filtering and sorting
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUsernameWithFilter(String username, String status, String startDate, String endDate, String sort) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderRepository.findByUser(user);
        
        // Apply filters
        List<Order> filteredOrders = orders.stream()
                .filter(order -> {
                    // Status filter
                    if (status != null && !status.trim().isEmpty()) {
                        try {
                            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                            if (order.getStatus() != orderStatus) {
                                return false;
                            }
                        } catch (IllegalArgumentException e) {
                            // Invalid status, skip this filter
                        }
                    }
                    
                    // Date range filter
                    if (startDate != null && !startDate.trim().isEmpty()) {
                        try {
                            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                            if (order.getOrderDate().toLocalDate().isBefore(start)) {
                                return false;
                            }
                        } catch (Exception e) {
                            // Invalid date format, skip this filter
                        }
                    }
                    
                    if (endDate != null && !endDate.trim().isEmpty()) {
                        try {
                            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
                            if (order.getOrderDate().toLocalDate().isAfter(end)) {
                                return false;
                            }
                        } catch (Exception e) {
                            // Invalid date format, skip this filter
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        // Apply sorting
        if (sort != null && sort.contains("orderDate")) {
            boolean ascending = sort.contains("asc");
            filteredOrders.sort((o1, o2) -> {
                int comparison = o1.getOrderDate().compareTo(o2.getOrderDate());
                return ascending ? comparison : -comparison;
            });
        } else {
            // Default: newest first
            filteredOrders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
        }
        
        return filteredOrders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all orders for a specific user by username with filtering, sorting and pagination
     * 
     * @param username Username
     * @param status Order status filter (optional)
     * @param startDate Start date filter (optional: yyyy-MM-dd)
     * @param endDate End date filter (optional: yyyy-MM-dd)
     * @param sort Sort option (orderDate,desc or orderDate,asc)
     * @param page Page number (0-based)
     * @param size Page size
     * @return Map with orders, pagination metadata
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getOrdersByUsernameWithFilterAndPagination(String username, String status, String startDate, String endDate, String sort, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> allOrders = orderRepository.findByUser(user);
        
        // Apply filters
        List<Order> filteredOrders = allOrders.stream()
                .filter(order -> {
                    // Status filter
                    if (status != null && !status.trim().isEmpty()) {
                        try {
                            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                            if (order.getStatus() != orderStatus) {
                                return false;
                            }
                        } catch (IllegalArgumentException e) {
                            // Invalid status, skip this filter
                        }
                    }
                    
                    // Date range filter
                    if (startDate != null && !startDate.trim().isEmpty()) {
                        try {
                            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                            if (order.getOrderDate().toLocalDate().isBefore(start)) {
                                return false;
                            }
                        } catch (Exception e) {
                            // Invalid date format, skip this filter
                        }
                    }
                    
                    if (endDate != null && !endDate.trim().isEmpty()) {
                        try {
                            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
                            if (order.getOrderDate().toLocalDate().isAfter(end)) {
                                return false;
                            }
                        } catch (Exception e) {
                            // Invalid date format, skip this filter
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        // Apply sorting
        if (sort != null && sort.contains("orderDate")) {
            boolean ascending = sort.contains("asc");
            filteredOrders.sort((o1, o2) -> {
                int comparison = o1.getOrderDate().compareTo(o2.getOrderDate());
                return ascending ? comparison : -comparison;
            });
        } else {
            // Default: newest first
            filteredOrders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
        }
        
        // Apply pagination
        int totalElements = filteredOrders.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        List<Order> pagedOrders = filteredOrders.subList(startIndex, endIndex);
        
        // Convert to DTOs
        List<OrderDto> orderDtos = pagedOrders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("content", orderDtos);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("currentPage", page);
        response.put("size", size);
        response.put("first", page == 0);
        response.put("last", page >= totalPages - 1);
        
        return response;
    }
} 