package com.shop.backend.controller;

import com.shop.backend.dto.ProductDto;
import com.shop.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Product operations
 * This controller provides CRUD operations for products
 * 
 * Features:
 * - Product listing (USER, ADMIN)
 * - Product creation (ADMIN only)
 * - Product updates (ADMIN only)
 * - Product deletion (ADMIN only)
 * - Role-based access control
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products with pagination, search, and filter support
     *
     * @param page Page number (0-based, default: 0)
     * @param size Page size (default: 12)
     * @param query Search query (optional)
     * @param category Category filter (optional)
     * @param price Price range filter (optional: 0-50, 50-100, 100+)
     * @param sort Sort option (name, price-low, price-high, newest)
     * @return Map with products and total count
     */
    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String price,
            @RequestParam(required = false, defaultValue = "name") String sort
    ) {
        return ResponseEntity.ok(productService.getProducts(page, size, query, category, price, sort));
    }

    /**
     * Get product by ID
     * Public access (no login required)
     * 
     * @param id Product ID
     * @return Product details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        Optional<ProductDto> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new product with optional image upload
     * Accessible by ADMIN role only
     *
     * @param productDto Product data (JSON, as a string)
     * @param imageFile  Image file (optional)
     * @return Created product
     */
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> createProductMultipart(
            @RequestPart("product") String productDtoJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        ProductDto productDto = productService.parseProductDto(productDtoJson);
        ProductDto createdProduct = productService.createProduct(productDto, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Update an existing product with optional image upload
     * Accessible by ADMIN role only
     *
     * @param id         Product ID
     * @param productDto Product data (JSON, as a string)
     * @param imageFile  Image file (optional)
     * @return Updated product
     */
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> updateProductMultipart(
            @PathVariable Long id,
            @RequestPart("product") String productDtoJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        ProductDto productDto = productService.parseProductDto(productDtoJson);
        ProductDto updatedProduct = productService.updateProduct(id, productDto, imageFile);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Delete a product
     * Accessible by ADMIN role only
     * 
     * @param id Product ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get products by category (public access)
     * 
     * @param category Product category
     * @return List of products in the specified category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable String category) {
        List<ProductDto> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    /**
     * Search products by name (public access)
     * 
     * @param query Search query
     * @return List of products matching the search criteria
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam String query) {
        List<ProductDto> products = productService.searchProducts(query);
        return ResponseEntity.ok(products);
    }

    /**
     * Get all categories (public access)
     * 
     * @return List of all unique categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
} 