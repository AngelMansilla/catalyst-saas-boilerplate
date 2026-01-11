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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Custom access denied handler that returns JSON error responses
 * when an authenticated user tries to access a resource without proper permissions.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    
    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    
    private final ObjectMapper objectMapper;
    
    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.warn("Access denied for request: {} {} - {}", 
            request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());
        
        var apiError = ApiError.builder()
            .code("AUTH.FORBIDDEN")
            .message("You do not have permission to access this resource")
            .status(HttpStatus.FORBIDDEN.value())
            .path(request.getRequestURI())
            .timestamp(Instant.now())
            .build();
        
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(apiError.toResponse()));
    }
}

