package com.shop.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for authentication response
 * This class is used for login and registration API responses
 * 
 * Features:
 * - JWT token for authentication
 * - User information
 * - Response status and message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String role;
    private String message;
    private boolean success;

    /**
     * Constructor for successful authentication
     * 
     * @param token JWT token
     * @param userId User ID
     * @param username Username
     * @param role User role
     */
    public AuthResponse(String token, Long userId, String username, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.success = true;
        this.message = "Authentication successful";
    }

    /**
     * Constructor for failed authentication
     * 
     * @param message Error message
     */
    public AuthResponse(String message) {
        this.message = message;
        this.success = false;
    }
} 