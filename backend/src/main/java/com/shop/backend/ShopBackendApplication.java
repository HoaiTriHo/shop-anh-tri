package com.shop.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the E-commerce Shop Backend
 * This class serves as the entry point for the Spring Boot application
 * 
 * Features included:
 * - Spring Boot auto-configuration
 * - Component scanning for all packages under com.shop.backend
 * - JPA repository scanning
 */
@SpringBootApplication
public class ShopBackendApplication {

    /**
     * Main method that starts the Spring Boot application
     * This method initializes the application context and starts the embedded server
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        // Start the Spring Boot application with the provided arguments
        SpringApplication.run(ShopBackendApplication.class, args);
        
        // Log successful startup
        System.out.println("🚀 E-commerce Shop Backend started successfully!");
        System.out.println("📡 API available at: http://localhost:8080/api");
        System.out.println("🗄️  Database: MySQL on localhost:3306/shop_db");
    }
} 