package com.shop.backend.controller;

import com.shop.backend.dto.AddToCartRequest;
import com.shop.backend.dto.CartDto;
import com.shop.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class CartController {
    private final CartService cartService;

    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<String> test(Authentication authentication) {
        log.info("Cart test endpoint called. Authentication: {}", authentication);
        if (authentication != null) {
            log.info("User: {}", authentication.getName());
        }
        return ResponseEntity.ok("Cart controller is working!");
    }

    // Lấy giỏ hàng của user hiện tại
    @GetMapping
    public ResponseEntity<CartDto> getCart(Authentication authentication) {
        log.info("Get cart called. Authentication: {}", authentication);
        if (authentication == null) {
            log.warn("No authentication found, returning empty cart");
            // Return empty cart if not authenticated
            CartDto emptyCart = new CartDto(1L, new ArrayList<>(), BigDecimal.ZERO);
            return ResponseEntity.ok(emptyCart);
        }
        String username = authentication.getName();
        log.info("Getting cart for user: {}", username);
        CartDto cart = cartService.getCartByUsername(username);
        return ResponseEntity.ok(cart);
    }

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(@RequestBody AddToCartRequest request, Authentication authentication) {
        log.info("Add to cart called. Authentication: {}, Request: {}", authentication, request);
        if (authentication == null) {
            log.warn("No authentication found for add to cart");
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        log.info("Adding to cart for user: {}", username);
        CartDto cart = cartService.addToCart(username, request);
        return ResponseEntity.ok(cart);
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    @PutMapping("/item/{itemId}")
    public ResponseEntity<CartDto> updateCartItem(@PathVariable Long itemId, @RequestParam int quantity, Authentication authentication) {
        log.info("Update cart item called. Authentication: {}, ItemId: {}, Quantity: {}", authentication, itemId, quantity);
        if (authentication == null) {
            log.warn("No authentication found for update cart item");
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        CartDto cart = cartService.updateCartItem(username, itemId, quantity);
        return ResponseEntity.ok(cart);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<CartDto> removeCartItem(@PathVariable Long itemId, Authentication authentication) {
        log.info("Remove cart item called. Authentication: {}, ItemId: {}", authentication, itemId);
        if (authentication == null) {
            log.warn("No authentication found for remove cart item");
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        CartDto cart = cartService.removeCartItem(username, itemId);
        return ResponseEntity.ok(cart);
    }

    // Xóa toàn bộ giỏ hàng
    @DeleteMapping
    public ResponseEntity<CartDto> clearCart(Authentication authentication) {
        log.info("Clear cart called. Authentication: {}", authentication);
        if (authentication == null) {
            log.warn("No authentication found for clear cart");
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        CartDto cart = cartService.clearCart(username);
        return ResponseEntity.ok(cart);
    }
} 