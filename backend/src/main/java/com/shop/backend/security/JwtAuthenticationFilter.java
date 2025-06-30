package com.shop.backend.security;

import com.shop.backend.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * This filter intercepts requests and validates JWT tokens
 * 
 * Features:
 * - JWT token validation
 * - User authentication setup
 * - Security context management
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        log.info("[JWT FILTER] Bắt đầu xử lý request: {} {}", request.getMethod(), request.getRequestURI());
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String username;
            log.debug("Authorization header: {}", authHeader);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No valid Authorization header found");
                filterChain.doFilter(request, response);
                log.info("[JWT FILTER] Kết thúc xử lý request: {} {}", request.getMethod(), request.getRequestURI());
                return;
            }
            jwt = authHeader.substring(7);
            log.debug("Extracted JWT token: {}", jwt.substring(0, Math.min(50, jwt.length())) + "...");
            if (jwtTokenProvider.validateToken(jwt)) {
                username = jwtTokenProvider.getUsernameFromToken(jwt);
                log.info("[JWT FILTER] Token hợp lệ, username giải mã: {}", username);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    log.info("[JWT FILTER] Loaded user details for: {}", username);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("[JWT FILTER] Set authentication for user: {}", username);
                }
            } else {
                log.warn("[JWT FILTER] Token KHÔNG hợp lệ hoặc hết hạn!");
            }
        } catch (Exception e) {
            log.error("[JWT FILTER] Lỗi khi xác thực JWT: {}", e.getMessage(), e);
        }
        log.info("[JWT FILTER] Kết thúc xử lý request: {} {}", request.getMethod(), request.getRequestURI());
        filterChain.doFilter(request, response);
    }
} 