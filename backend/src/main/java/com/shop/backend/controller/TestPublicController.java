package com.shop.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-public")
public class TestPublicController {
    @GetMapping
    public String test() {
        return "PUBLIC_OK";
    }
} 