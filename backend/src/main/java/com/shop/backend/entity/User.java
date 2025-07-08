package com.shop.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;

/**
 * User entity representing a user in the e-commerce system
 * This entity is used for authentication, authorization, and user management
 * 
 * Features:
 * - JPA entity mapping to database table
 * - Spring Security UserDetails implementation for authentication
 * - Input validation using Bean Validation
 * - Lombok annotations for reducing boilerplate code
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    // Role field for authorization - default to USER role
    @Column(nullable = false)
    private String role = "USER";

    // Account status fields
    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "is_account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    @Column(name = "is_account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "is_credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Automatically set createdAt before inserting a new user.
     * This ensures the created_at column is never null.
     * This method is called by JPA before persisting a new entity.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor for creating a new user with basic information
     * This constructor is used when registering new users
     * 
     * @param username Unique username for the user
     * @param email Unique email address for the user
     * @param password Encrypted password for the user
     * @param firstName User's first name
     * @param lastName User's last name
     */
    public User(String username, String email, String password, String firstName, String lastName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Get authorities for Spring Security
     * This method returns the user's role as a Spring Security authority
     * 
     * @return Collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    /**
     * Check if account is not expired
     * 
     * @return true if account is not expired
     */
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    /**
     * Check if account is not locked
     * 
     * @return true if account is not locked
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * Check if credentials are not expired
     * 
     * @return true if credentials are not expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    /**
     * Check if account is enabled
     * 
     * @return true if account is enabled
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get full name of the user
     * 
     * @return Concatenated first and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
} 