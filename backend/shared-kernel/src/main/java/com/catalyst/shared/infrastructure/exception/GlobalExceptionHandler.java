package com.catalyst.shared.infrastructure.exception;

import com.catalyst.shared.application.dto.ApiError;
import com.catalyst.shared.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.catalyst.shared.infrastructure.config.ErrorProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler that converts exceptions to standardized API responses.
 */
@RestControllerAdvice
@EnableConfigurationProperties(ErrorProperties.class)
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    private final ErrorProperties errorProperties;
    
    public GlobalExceptionHandler(ErrorProperties errorProperties) {
        this.errorProperties = errorProperties;
    }
    
    // ==================== Domain Exceptions ====================
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        return handleDomainException(ex, request, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEntityAlreadyExists(
            EntityAlreadyExistsException ex, HttpServletRequest request) {
        return handleDomainException(ex, request, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            ValidationException ex, HttpServletRequest request) {
        return handleDomainException(ex, request, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        return handleDomainException(ex, request, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorization(
            AuthorizationException ex, HttpServletRequest request) {
        return handleDomainException(ex, request, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(
            BusinessRuleViolationException ex, HttpServletRequest request) {
        return handleDomainException(ex, request, HttpStatus.valueOf(422));
    }
    
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(
            RateLimitExceededException ex, HttpServletRequest request) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        headers.add("X-RateLimit-Reset", String.valueOf(ex.getResetTime().getEpochSecond()));
        
        var apiError = createApiError(ex, request);
        
        log.warn("Rate limit exceeded: {} - Path: {}", 
            ex.getMessage(), request.getRequestURI());
        
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .headers(headers)
            .body(apiError.toResponse());
    }
    
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Map<String, Object>> handleExternalService(
            ExternalServiceException ex, HttpServletRequest request) {
        
        HttpStatus status = ex.isRetryable() 
            ? HttpStatus.SERVICE_UNAVAILABLE 
            : HttpStatus.BAD_GATEWAY;
        
        log.error("External service error: {} - {}", ex.getServiceName(), ex.getMessage());
        
        return handleDomainException(ex, request, status);
    }
    
    // ==================== Spring/Validation Exceptions ====================
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage() != null 
                    ? error.getDefaultMessage() 
                    : "Invalid value",
                (existing, replacement) -> existing
            ));
        
        var apiError = ApiError.builder()
            .code("VALIDATION.FAILED")
            .message("Validation failed for one or more fields")
            .status(HttpStatus.BAD_REQUEST.value())
            .path(request.getRequestURI())
            .timestamp(Instant.now())
            .addDetail("fieldErrors", fieldErrors)
            .build();
        
        log.debug("Validation failed: {}", fieldErrors);
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(apiError.toResponse());
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        Map<String, String> violations = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            violations.put(path, message);
        });
        
        var apiError = ApiError.builder()
            .code("VALIDATION.CONSTRAINT_VIOLATION")
            .message("Constraint validation failed")
            .status(HttpStatus.BAD_REQUEST.value())
            .path(request.getRequestURI())
            .timestamp(Instant.now())
            .addDetail("violations", violations)
            .build();
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(apiError.toResponse());
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String paramName = ex.getName();
        String requiredType = ex.getRequiredType() != null 
            ? ex.getRequiredType().getSimpleName() 
            : "unknown";
        
        var apiError = ApiError.builder()
            .code("VALIDATION.TYPE_MISMATCH")
            .message(String.format("Parameter '%s' must be of type %s", paramName, requiredType))
            .status(HttpStatus.BAD_REQUEST.value())
            .path(request.getRequestURI())
            .timestamp(Instant.now())
            .addDetail("parameter", paramName)
            .addDetail("requiredType", requiredType)
            .build();
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(apiError.toResponse());
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        
        var apiError = ApiError.builder()
            .code("AUTH.FORBIDDEN")
            .message("You do not have permission to access this resource")
            .status(HttpStatus.FORBIDDEN.value())
            .path(request.getRequestURI())
            .timestamp(Instant.now())
            .build();
        
        log.warn("Access denied: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(apiError.toResponse());
    }
    
    // ==================== Fallback Handler ====================
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        String correlationId = UUID.randomUUID().toString();
        
        log.error("Unhandled exception [correlationId={}]: {}", correlationId, ex.getMessage(), ex);
        
        var builder = ApiError.builder()
            .code("INTERNAL.SERVER_ERROR")
            .message("An unexpected error occurred. Please try again later.")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .path(request.getRequestURI())
            .timestamp(Instant.now())
            .addDetail("correlationId", correlationId);
        
        if (errorProperties.isIncludeStacktrace()) {
            builder.addDetail("exception", ex.getClass().getName());
            builder.addDetail("exceptionMessage", ex.getMessage());
        }
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(builder.build().toResponse());
    }
    
    // ==================== Helper Methods ====================
    
    private ResponseEntity<Map<String, Object>> handleDomainException(
            DomainException ex, HttpServletRequest request, HttpStatus status) {
        
        var apiError = createApiError(ex, request);
        
        if (status.is5xxServerError()) {
            log.error("Domain exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        } else {
            log.debug("Domain exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        }
        
        return ResponseEntity.status(status).body(apiError.toResponse());
    }
    
    private ApiError createApiError(DomainException ex, HttpServletRequest request) {
        return ApiError.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .status(ex.getHttpStatus())
            .path(request.getRequestURI())
            .timestamp(ex.getTimestamp())
            .details(ex.getDetails())
            .build();
    }
}

