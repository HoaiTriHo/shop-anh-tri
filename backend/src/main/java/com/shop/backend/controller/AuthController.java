package com.shop.backend.controller;

import com.shop.backend.dto.AuthResponse;
import com.shop.backend.dto.LoginRequest;
import com.shop.backend.dto.RegisterRequest;
import com.shop.backend.entity.User;
import com.shop.backend.security.JwtTokenProvider;
import com.shop.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Authentication operations
 * This controller provides user registration and login endpoints
 * 
 * Features:
 * - User registration with role USER
 * - User login with JWT token generation
 * - Input validation
 * - Proper HTTP status codes
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user
     * 
     * @param request Registration request
     * @return Authentication response with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Register the user
            User user = userService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()
            );

            // Generate JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("role", user.getRole());
            
            String token = tokenProvider.generateToken(user.getUsername(), claims);

            // Create response
            AuthResponse response = new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            AuthResponse response = new AuthResponse(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Login user and return JWT token
     * 
     * @param request Login request
     * @return Authentication response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            User user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("role", user.getRole());
            
            String token = tokenProvider.generateToken(user.getUsername(), claims);

            // Create response
            AuthResponse response = new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            AuthResponse response = new AuthResponse("Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Get current user information
     * 
     * @return Current user information
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.findByUsername(authentication.getName())
                    .orElse(null);
            
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("firstName", user.getFirstName());
                userInfo.put("lastName", user.getLastName());
                userInfo.put("role", user.getRole());
                
                return ResponseEntity.ok(userInfo);
            }
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
} 