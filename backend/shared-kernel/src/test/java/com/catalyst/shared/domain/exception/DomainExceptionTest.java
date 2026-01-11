package com.catalyst.shared.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for domain exceptions.
 */
class DomainExceptionTest {
    
    @Test
    @DisplayName("EntityNotFoundException should have correct HTTP status")
    void entityNotFoundExceptionShouldHaveCorrectStatus() {
        // Given
        EntityNotFoundException ex = new EntityNotFoundException("User", "123");
        
        // Then
        assertThat(ex.getHttpStatus()).isEqualTo(404);
        assertThat(ex.getErrorCode()).isEqualTo("USER.NOT_FOUND");
        assertThat(ex.getMessage()).contains("User");
        assertThat(ex.getMessage()).contains("123");
    }
    
    @Test
    @DisplayName("EntityAlreadyExistsException should have correct HTTP status")
    void entityAlreadyExistsExceptionShouldHaveCorrectStatus() {
        // Given
        EntityAlreadyExistsException ex = 
            EntityAlreadyExistsException.emailAlreadyExists("test@example.com");
        
        // Then
        assertThat(ex.getHttpStatus()).isEqualTo(409);
        assertThat(ex.getErrorCode()).isEqualTo("USER.ALREADY_EXISTS");
    }
    
    @Test
    @DisplayName("ValidationException should include field errors")
    void validationExceptionShouldIncludeFieldErrors() {
        // Given
        ValidationException ex = ValidationException.required("email");
        
        // Then
        assertThat(ex.getHttpStatus()).isEqualTo(400);
        assertThat(ex.getFieldErrors()).hasSize(1);
        assertThat(ex.getFieldErrors().get(0).field()).isEqualTo("email");
    }
    
    @Test
    @DisplayName("AuthenticationException factory methods should work correctly")
    void authenticationExceptionFactoryMethods() {
        // Given/When/Then
        assertThat(AuthenticationException.invalidCredentials().getErrorCode())
            .isEqualTo("AUTH.LOGIN.INVALID_CREDENTIALS");
        assertThat(AuthenticationException.tokenExpired().getErrorCode())
            .isEqualTo("AUTH.TOKEN.EXPIRED");
        assertThat(AuthenticationException.invalidToken().getErrorCode())
            .isEqualTo("AUTH.TOKEN.INVALID");
        
        // All should be 401
        assertThat(AuthenticationException.invalidCredentials().getHttpStatus()).isEqualTo(401);
    }
    
    @Test
    @DisplayName("AuthorizationException should have correct HTTP status")
    void authorizationExceptionShouldHaveCorrectStatus() {
        // Given
        AuthorizationException ex = AuthorizationException.roleRequired("ADMIN");
        
        // Then
        assertThat(ex.getHttpStatus()).isEqualTo(403);
        assertThat(ex.getDetails()).containsKey("requiredRole");
    }
    
    @Test
    @DisplayName("BusinessRuleViolationException should have correct HTTP status")
    void businessRuleViolationExceptionShouldHaveCorrectStatus() {
        // Given
        BusinessRuleViolationException ex = 
            BusinessRuleViolationException.quotaExceeded("documents", 100);
        
        // Then
        assertThat(ex.getHttpStatus()).isEqualTo(422);
        assertThat(ex.getDetails()).containsEntry("resource", "documents");
        assertThat(ex.getDetails()).containsEntry("limit", 100);
    }
    
    @Test
    @DisplayName("RateLimitExceededException should include retry information")
    void rateLimitExceededExceptionShouldIncludeRetryInfo() {
        // Given
        RateLimitExceededException ex = RateLimitExceededException.withRetryAfter(60);
        
        // Then
        assertThat(ex.getHttpStatus()).isEqualTo(429);
        assertThat(ex.getRetryAfterSeconds()).isEqualTo(60);
        assertThat(ex.getResetTime()).isNotNull();
    }
    
    @Test
    @DisplayName("ExternalServiceException should indicate retryability")
    void externalServiceExceptionShouldIndicateRetryability() {
        // Given
        ExternalServiceException retryable = 
            ExternalServiceException.stripeError("Timeout");
        ExternalServiceException nonRetryable = 
            ExternalServiceException.stripePaymentFailed("Card declined", "insufficient_funds");
        
        // Then
        assertThat(retryable.isRetryable()).isTrue();
        assertThat(retryable.getHttpStatus()).isEqualTo(503);
        
        assertThat(nonRetryable.isRetryable()).isFalse();
        assertThat(nonRetryable.getHttpStatus()).isEqualTo(502);
    }
    
    @Test
    @DisplayName("DomainException should extract category from error code")
    void domainExceptionShouldExtractCategory() {
        // Given
        AuthenticationException ex = AuthenticationException.tokenExpired();
        
        // Then
        assertThat(ex.getCategory()).isEqualTo("AUTH");
    }
    
    @Test
    @DisplayName("DomainException should allow adding details")
    void domainExceptionShouldAllowAddingDetails() {
        // Given
        EntityNotFoundException ex = new EntityNotFoundException("User", "123");
        
        // When
        ex.addDetail("attemptedAt", "2026-01-11");
        
        // Then
        assertThat(ex.getDetails()).containsEntry("attemptedAt", "2026-01-11");
    }
}

