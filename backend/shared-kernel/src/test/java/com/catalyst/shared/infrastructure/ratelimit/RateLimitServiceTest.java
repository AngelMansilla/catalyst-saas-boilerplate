package com.catalyst.shared.infrastructure.ratelimit;

import com.catalyst.shared.domain.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for RateLimitService.
 */
class RateLimitServiceTest {
    
    private RateLimitService rateLimitService;
    private RateLimitProperties properties;
    
    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setEnabled(true);
        
        // Set up test tiers with small limits
        RateLimitProperties.TierConfig anonymous = new RateLimitProperties.TierConfig();
        anonymous.setRequestsPerMinute(5);
        anonymous.setBurst(2);
        anonymous.setWindow(Duration.ofMinutes(1));
        properties.setAnonymous(anonymous);
        
        RateLimitProperties.TierConfig authenticated = new RateLimitProperties.TierConfig();
        authenticated.setRequestsPerMinute(10);
        authenticated.setBurst(5);
        authenticated.setWindow(Duration.ofMinutes(1));
        properties.setAuthenticated(authenticated);
        
        rateLimitService = new RateLimitService(properties);
    }
    
    @Test
    @DisplayName("Should allow requests within limit")
    void shouldAllowRequestsWithinLimit() {
        // Given
        String key = "test-user-1";
        
        // When
        RateLimitService.RateLimitResult result = 
            rateLimitService.tryConsume(key, RateLimitTier.AUTHENTICATED);
        
        // Then
        assertThat(result.isAllowed()).isTrue();
        assertThat(result.remaining()).isLessThanOrEqualTo(result.limit());
    }
    
    @Test
    @DisplayName("Should deny requests exceeding limit")
    void shouldDenyRequestsExceedingLimit() {
        // Given
        String key = "test-user-2";
        
        // When - exhaust the burst capacity
        for (int i = 0; i < 3; i++) {
            rateLimitService.tryConsume(key, RateLimitTier.ANONYMOUS);
        }
        
        // Then - next request should be denied
        RateLimitService.RateLimitResult result = 
            rateLimitService.tryConsume(key, RateLimitTier.ANONYMOUS);
        
        assertThat(result.isAllowed()).isFalse();
        assertThat(result.retryAfterSeconds()).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("Should throw exception when using consumeOrThrow")
    void shouldThrowExceptionWhenExceeded() {
        // Given
        String key = "test-user-3";
        
        // Exhaust the limit
        for (int i = 0; i < 3; i++) {
            rateLimitService.tryConsume(key, RateLimitTier.ANONYMOUS);
        }
        
        // When/Then
        assertThatThrownBy(() -> 
            rateLimitService.consumeOrThrow(key, RateLimitTier.ANONYMOUS))
            .isInstanceOf(RateLimitExceededException.class);
    }
    
    @Test
    @DisplayName("Should return allowed when disabled")
    void shouldAllowWhenDisabled() {
        // Given
        properties.setEnabled(false);
        RateLimitService disabledService = new RateLimitService(properties);
        
        // When
        RateLimitService.RateLimitResult result = 
            disabledService.tryConsume("any-key", RateLimitTier.ANONYMOUS);
        
        // Then
        assertThat(result.isAllowed()).isTrue();
    }
    
    @Test
    @DisplayName("Should use different buckets for different keys")
    void shouldUseDifferentBucketsForDifferentKeys() {
        // Given
        String key1 = "user-1";
        String key2 = "user-2";
        
        // When - exhaust key1
        for (int i = 0; i < 3; i++) {
            rateLimitService.tryConsume(key1, RateLimitTier.ANONYMOUS);
        }
        
        // Then - key2 should still be allowed
        RateLimitService.RateLimitResult result = 
            rateLimitService.tryConsume(key2, RateLimitTier.ANONYMOUS);
        
        assertThat(result.allowed()).isTrue();
    }
    
    @Test
    @DisplayName("Should use different limits for different tiers")
    void shouldUseDifferentLimitsForDifferentTiers() {
        // Given
        String key = "same-user";
        
        // When - check authenticated tier limit
        RateLimitService.RateLimitResult authenticatedResult = 
            rateLimitService.tryConsume(key, RateLimitTier.AUTHENTICATED);
        
        // Then - authenticated has higher limit
        assertThat(authenticatedResult.limit()).isEqualTo(10);
    }
    
    @Test
    @DisplayName("Should clear buckets")
    void shouldClearBuckets() {
        // Given
        String key = "test-user";
        
        // Exhaust the limit
        for (int i = 0; i < 3; i++) {
            rateLimitService.tryConsume(key, RateLimitTier.ANONYMOUS);
        }
        
        // Verify denied
        assertThat(rateLimitService.tryConsume(key, RateLimitTier.ANONYMOUS).allowed())
            .isFalse();
        
        // When - clear buckets
        rateLimitService.clearBuckets();
        
        // Then - should be allowed again
        assertThat(rateLimitService.tryConsume(key, RateLimitTier.ANONYMOUS).allowed())
            .isTrue();
    }
}

