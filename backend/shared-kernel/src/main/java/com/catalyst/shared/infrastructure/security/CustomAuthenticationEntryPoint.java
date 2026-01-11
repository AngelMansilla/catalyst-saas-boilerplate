package com.catalyst.shared.infrastructure.security;

import com.catalyst.shared.application.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Custom authentication entry point that returns JSON error responses
 * when authentication is required but not provided or invalid.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);
    
    private final ObjectMapper objectMapper;
    
    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        
        log.debug("Authentication failed for request: {} - {}", 
            request.getMethod(), request.getRequestURI());
        
        var apiError = ApiError.builder()
            .code("AUTH.UNAUTHORIZED")
            .message("Authentication is required to access this resource")
            .status(HttpStatus.UNAUTHORIZED.value())
            .path(request.getRequestURI())
            .timestamp(Instant.now())
            .build();
        
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(apiError.toResponse()));
    }
}

