package com.shop.backend.service;

import com.shop.backend.dto.AddToCartRequest;
import com.shop.backend.dto.CartDto;
import com.shop.backend.dto.CartItemDto;
import com.shop.backend.dto.ProductDto;
import com.shop.backend.entity.Cart;
import com.shop.backend.entity.CartItem;
import com.shop.backend.entity.Product;
import com.shop.backend.entity.User;
import com.shop.backend.repository.CartItemRepository;
import com.shop.backend.repository.CartRepository;
import com.shop.backend.repository.ProductRepository;
import com.shop.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartDto getCartByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> createCartForUser(user));
        return convertToDto(cart);
    }

    public CartDto addToCart(String username, AddToCartRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if product is active
        if (!product.isActive()) {
            throw new RuntimeException("Product is not available for purchase");
        }
        
        // Check stock availability
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity() + ", Requested: " + request.getQuantity());
        }
        
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> createCartForUser(user));
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndProduct(cart, product);
        
        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            
            // Check total quantity against stock
            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity() + ", Total in cart: " + newQuantity);
            }
            
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
            item.setPrice(product.getPrice());
            cartItemRepository.save(item);
            cart.getItems().add(item);
        }
        updateCartTotal(cart);
        cartRepository.save(cart);
        return convertToDto(cart);
    }

    public CartDto updateCartItem(String username, Long itemId, int quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Cart not found"));
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        if (item.getCart().getId().equals(cart.getId())) {
            // Check stock availability
            Product product = item.getProduct();
            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity() + ", Requested: " + quantity);
            }
            
            item.setQuantity(quantity);
            cartItemRepository.save(item);
            updateCartTotal(cart);
            cartRepository.save(cart);
        }
        return convertToDto(cart);
    }

    public CartDto removeCartItem(String username, Long itemId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Cart not found"));
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        if (item.getCart().getId().equals(cart.getId())) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            updateCartTotal(cart);
            cartRepository.save(cart);
        }
        return convertToDto(cart);
    }

    private Cart createCartForUser(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalPrice(BigDecimal.ZERO);
        return cartRepository.save(cart);
    }

    private void updateCartTotal(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
    }

    private CartDto convertToDto(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());
        return new CartDto(cart.getId(), items, cart.getTotalPrice());
    }

    private CartItemDto convertToItemDto(CartItem item) {
        ProductDto productDto = new ProductDto(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getDescription(),
                item.getProduct().getPrice(),
                item.getProduct().getImageUrl(),
                item.getProduct().getCategory(),
                item.getProduct().getBrand(),
                item.getProduct().getStockQuantity(),
                item.getProduct().isActive(),
                null
        );
        return new CartItemDto(item.getId(), productDto, item.getQuantity(), item.getPrice());
    }
} 