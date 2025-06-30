package com.shop.backend.service;

import com.shop.backend.dto.ProductDto;
import com.shop.backend.entity.Product;
import com.shop.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

/**
 * Service class for Product business logic
 * This class handles all product-related operations
 * 
 * Features:
 * - CRUD operations for products
 * - Business logic validation
 * - Transaction management
 * - DTO conversion
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private static final String UPLOAD_DIR = "uploads";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get all products with pagination
     * 
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of products for the specified page
     */
    public List<ProductDto> getAllProducts(int page, int size) {
        return productRepository.findByActiveTrue()
                .stream()
                .skip(page * size)
                .limit(size)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all products (for backward compatibility)
     * 
     * @return List of all products
     */
    public List<ProductDto> getAllProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     * 
     * @param id Product ID
     * @return Optional containing the product if found
     */
    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * Parse ProductDto from JSON string
     * @param json JSON string
     * @return ProductDto
     */
    public ProductDto parseProductDto(String json) {
        try {
            return objectMapper.readValue(json, ProductDto.class);
        } catch (IOException e) {
            throw new RuntimeException("Invalid product JSON", e);
        }
    }

    /**
     * Create a new product with optional image upload
     * @param productDto Product data
     * @param imageFile  Image file (optional)
     * @return Created product DTO
     */
    public ProductDto createProduct(ProductDto productDto, MultipartFile imageFile) {
        Product product = convertToEntity(productDto);
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = saveImage(imageFile);
            product.setImageUrl(imageUrl);
        }
        Product saved = productRepository.save(product);
        return convertToDto(saved);
    }

    /**
     * Update an existing product with optional image upload
     * @param id         Product ID
     * @param productDto Product data
     * @param imageFile  Image file (optional)
     * @return Updated product DTO
     */
    public ProductDto updateProduct(Long id, ProductDto productDto, MultipartFile imageFile) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        // Update fields
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        // Only update image if a new file is provided
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = saveImage(imageFile);
            product.setImageUrl(imageUrl);
        }
        Product saved = productRepository.save(product);
        return convertToDto(saved);
    }

    /**
     * Delete a product
     * 
     * @param id Product ID
     * @return true if product was deleted, false if not found
     */
    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Get products by category
     * 
     * @param category Product category
     * @return List of products in the specified category
     */
    public List<ProductDto> getProductsByCategory(String category) {
        return productRepository.findByCategory(category)
                .stream()
                .filter(Product::isActive)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Search products by name
     * 
     * @param searchTerm Search term
     * @return List of products matching the search criteria
     */
    public List<ProductDto> searchProducts(String searchTerm) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchTerm)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all categories
     * 
     * @return List of all unique categories
     */
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    /**
     * Get products with pagination, search, filter, and sort
     *
     * @param page Page number (0-based)
     * @param size Page size
     * @param query Search query
     * @param category Category filter
     * @param price Price range filter (0-50, 50-100, 100+)
     * @param sort Sort option (name, price-low, price-high, newest)
     * @return Map with products and total count
     */
    public Map<String, Object> getProducts(int page, int size, String query, String category, String price, String sort) {
        List<Product> all = productRepository.findByActiveTrue();
        // Filter by search query
        if (query != null && !query.isEmpty()) {
            String q = query.toLowerCase();
            all = all.stream().filter(p ->
                p.getName().toLowerCase().contains(q) ||
                p.getDescription().toLowerCase().contains(q)
            ).collect(Collectors.toList());
        }
        // Filter by category
        if (category != null && !category.isEmpty()) {
            all = all.stream().filter(p -> category.equalsIgnoreCase(p.getCategory())).collect(Collectors.toList());
        }
        // Filter by price (BigDecimal)
        if (price != null && !price.isEmpty()) {
            switch (price) {
                case "0-50":
                    all = all.stream().filter(p -> p.getPrice() != null && p.getPrice().compareTo(java.math.BigDecimal.valueOf(50)) <= 0).collect(Collectors.toList());
                    break;
                case "50-100":
                    all = all.stream().filter(p -> p.getPrice() != null && p.getPrice().compareTo(java.math.BigDecimal.valueOf(50)) > 0 && p.getPrice().compareTo(java.math.BigDecimal.valueOf(100)) <= 0).collect(Collectors.toList());
                    break;
                case "100+":
                    all = all.stream().filter(p -> p.getPrice() != null && p.getPrice().compareTo(java.math.BigDecimal.valueOf(100)) > 0).collect(Collectors.toList());
                    break;
            }
        }
        // Sort
        if (sort != null) {
            switch (sort) {
                case "price-low":
                    all = all.stream().sorted(Comparator.comparing(Product::getPrice, Comparator.nullsLast(java.math.BigDecimal::compareTo))).collect(Collectors.toList());
                    break;
                case "price-high":
                    all = all.stream().sorted(Comparator.comparing(Product::getPrice, Comparator.nullsLast(java.math.BigDecimal::compareTo)).reversed()).collect(Collectors.toList());
                    break;
                case "newest":
                    all = all.stream().sorted(Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
                    break;
                default:
                    all = all.stream().sorted(Comparator.comparing(Product::getName)).collect(Collectors.toList());
            }
        }
        int total = all.size();
        List<ProductDto> products = all.stream()
                .skip(page * size)
                .limit(size)
                .map(this::convertToDto)
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("total", total);
        return result;
    }

    /**
     * Convert Product entity to ProductDto
     * 
     * @param product Product entity
     * @return ProductDto
     */
    private ProductDto convertToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getCategory(),
                product.getBrand(),
                product.getStockQuantity(),
                product.isActive()
        );
    }

    /**
     * Convert ProductDto to Product entity
     * 
     * @param productDto ProductDto
     * @return Product entity
     */
    private Product convertToEntity(ProductDto productDto) {
        return new Product(
                productDto.getName(),
                productDto.getDescription(),
                productDto.getPrice(),
                productDto.getStockQuantity(),
                productDto.getCategory(),
                productDto.getBrand()
        );
    }

    /**
     * Save image file to uploads directory and return public URL
     * @param file MultipartFile
     * @return Public image URL
     */
    private String saveImage(MultipartFile file) {
        try {
            // Ensure uploads directory exists
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            // Generate unique filename
            String ext = getFileExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            Files.write(filePath, file.getBytes());
            // Return public URL path
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }

    /**
     * Get file extension from filename
     * @param filename Filename
     * @return Extension (without dot)
     */
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot + 1) : "";
    }
} 