package com.shop.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check controller for monitoring application status
 * This controller provides endpoints to check if the application is running
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Simple health check endpoint
     * Returns a simple message to indicate the application is running
     * 
     * @return Health status message
     */
    @GetMapping
    public String health() {
        return "OK";
    }

    /**
     * Detailed health check endpoint
     * Returns more detailed information about the application status
     * 
     * @return Detailed health status
     */
    @GetMapping("/detailed")
    public HealthStatus detailedHealth() {
        return new HealthStatus("UP", "Application is running successfully");
    }

    /**
     * Health status response class
     */
    public static class HealthStatus {
        private String status;
        private String message;

        public HealthStatus(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
} 