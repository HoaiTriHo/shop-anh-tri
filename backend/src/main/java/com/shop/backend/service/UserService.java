package com.shop.backend.service;

import com.shop.backend.entity.User;
import com.shop.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service class for User business logic
 * This class handles user management and authentication
 * 
 * Features:
 * - User registration and management
 * - Password encoding
 * - Transaction management
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new user
     * 
     * @param username Username
     * @param email Email address
     * @param password Plain text password
     * @param firstName First name
     * @param lastName Last name
     * @return Created user
     */
    public User registerUser(String username, String email, String password, String firstName, String lastName) {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user with USER role
        User user = new User(username, email, passwordEncoder.encode(password), firstName, lastName);
        user.setRole("USER"); // Default role for new registrations
        
        return userRepository.save(user);
    }

    /**
     * Find user by username
     * 
     * @param username Username to search for
     * @return Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by ID
     * 
     * @param id User ID
     * @return Optional containing the user if found
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Check if username exists
     * 
     * @param username Username to check
     * @return true if username exists
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     * 
     * @param email Email to check
     * @return true if email exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
} 