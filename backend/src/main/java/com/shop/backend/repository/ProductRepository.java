package com.shop.backend.repository;

import com.shop.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity
 * This interface provides data access methods for product operations
 * 
 * Features:
 * - Extends JpaRepository for basic CRUD operations
 * - Custom query methods for product search and filtering
 * - Pagination support for large product catalogs
 * - Spring Data JPA automatic query generation
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by name
     * This method is used for product search functionality
     * 
     * @param name The product name to search for
     * @return Optional containing the product if found
     */
    Optional<Product> findByName(String name);

    /**
     * Find active products
     * This method returns only products that are currently available
     * 
     * @return List of active products
     */
    List<Product> findByActiveTrue();

    /**
     * Find products by category
     * This method is used for category-based product browsing
     * 
     * @param category The category to filter by
     * @return List of products in the specified category
     */
    List<Product> findByCategory(String category);

    /**
     * Find products by brand
     * This method is used for brand-based product filtering
     * 
     * @param brand The brand to filter by
     * @return List of products from the specified brand
     */
    List<Product> findByBrand(String brand);

    /**
     * Find products in stock
     * This method returns only products with available inventory
     * 
     * @return List of products that are in stock
     */
    List<Product> findByStockQuantityGreaterThan(Integer quantity);

    /**
     * Find products by price range
     * This method is used for price-based filtering
     * 
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @return List of products within the price range
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find products with pagination
     * This method is used for displaying products with pagination
     * 
     * @param pageable Pagination information
     * @return Page of products
     */
    Page<Product> findByActiveTrue(Pageable pageable);

    /**
     * Find products by category with pagination
     * This method is used for category browsing with pagination
     * 
     * @param category The category to filter by
     * @param pageable Pagination information
     * @return Page of products in the specified category
     */
    Page<Product> findByCategoryAndActiveTrue(String category, Pageable pageable);

    /**
     * Search products by name containing the search term
     * This method is used for product search functionality
     * 
     * @param searchTerm The term to search for in product names
     * @return List of products matching the search criteria
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.active = true")
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(@Param("searchTerm") String searchTerm);

    /**
     * Search products by name or description containing the search term
     * This method provides more comprehensive search functionality
     * 
     * @param searchTerm The term to search for
     * @return List of products matching the search criteria
     */
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.active = true")
    List<Product> findByNameOrDescriptionContainingIgnoreCaseAndActiveTrue(@Param("searchTerm") String searchTerm);

    /**
     * Find products with low stock
     * This method is used for inventory management
     * 
     * @param threshold The stock threshold
     * @return List of products with stock below the threshold
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.active = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    /**
     * Find products by multiple categories
     * This method is used for filtering by multiple categories
     * 
     * @param categories List of categories to filter by
     * @return List of products in any of the specified categories
     */
    @Query("SELECT p FROM Product p WHERE p.category IN :categories AND p.active = true")
    List<Product> findByCategoriesAndActiveTrue(@Param("categories") List<String> categories);

    /**
     * Find products by price range with pagination
     * This method is used for price filtering with pagination
     * 
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @param pageable Pagination information
     * @return Page of products within the price range
     */
    Page<Product> findByPriceBetweenAndActiveTrue(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Count products by category
     * This method is used for analytics and reporting
     * 
     * @param category The category to count
     * @return Number of products in the specified category
     */
    long countByCategory(String category);

    /**
     * Find all unique categories
     * This method is used for category navigation
     * 
     * @return List of unique category names
     */
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.active = true")
    List<String> findAllCategories();

    /**
     * Find all unique brands
     * This method is used for brand navigation
     * 
     * @return List of unique brand names
     */
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL AND p.active = true")
    List<String> findAllBrands();
} 