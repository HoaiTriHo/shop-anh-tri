package com.shop.backend.repository;

import com.shop.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 * This interface provides data access methods for user operations
 * 
 * Features:
 * - Extends JpaRepository for basic CRUD operations
 * - Custom query methods for specific user operations
 * - Spring Data JPA automatic query generation
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username
     * This method is used for authentication purposes
     * 
     * @param username The username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email address
     * This method is used for email-based operations
     * 
     * @param email The email address to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     * This method is used for validation during user registration
     * 
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * This method is used for validation during user registration
     * 
     * @param email The email address to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role
     * This method is used for administrative purposes
     * 
     * @param role The role to search for
     * @return List of users with the specified role
     */
    List<User> findByRole(String role);

    /**
     * Find enabled users
     * This method returns only active users
     * 
     * @return List of enabled users
     */
    List<User> findByEnabledTrue();

    /**
     * Find users by first name or last name containing the search term
     * This method is used for user search functionality
     * 
     * @param searchTerm The term to search for in names
     * @return List of users matching the search criteria
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    /**
     * Find users by email domain
     * This method is used for filtering users by email provider
     * 
     * @param domain The email domain to search for
     * @return List of users with the specified email domain
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:domain")
    List<User> findByEmailDomain(@Param("domain") String domain);

    /**
     * Count users by role
     * This method is used for analytics and reporting
     * 
     * @param role The role to count
     * @return Number of users with the specified role
     */
    long countByRole(String role);

    /**
     * Find users created after a specific date
     * This method is used for user analytics and reporting
     * 
     * @param date The date to filter from
     * @return List of users created after the specified date
     */
    @Query("SELECT u FROM User u WHERE u.id IN (SELECT o.user.id FROM Order o WHERE o.orderDate >= :date)")
    List<User> findUsersWithOrdersAfter(@Param("date") java.time.LocalDateTime date);
} 