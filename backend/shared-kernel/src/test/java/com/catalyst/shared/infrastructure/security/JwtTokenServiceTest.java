package com.catalyst.shared.infrastructure.security;

import com.catalyst.shared.domain.auth.JwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtTokenService.
 */
class JwtTokenServiceTest {
    
    private JwtTokenService jwtTokenService;
    private JwtProperties jwtProperties;
    
    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-for-unit-tests-32-chars!");
        jwtProperties.setAccessTokenExpiration(Duration.ofMinutes(15));
        jwtProperties.setRefreshTokenExpiration(Duration.ofDays(7));
        jwtProperties.setIssuer("test-issuer");
        jwtProperties.setAudience("test-audience");
        
        jwtTokenService = new JwtTokenService(jwtProperties);
    }
    
    @Test
    @DisplayName("Should generate valid access token")
    void shouldGenerateValidAccessToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Set<String> roles = Set.of("ROLE_USER");
        
        // When
        String token = jwtTokenService.generateAccessToken(userId, email, roles);
        
        // Then
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }
    
    @Test
    @DisplayName("Should validate and extract claims from token")
    void shouldValidateAndExtractClaims() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Set<String> roles = Set.of("ROLE_USER", "ROLE_ADMIN");
        
        String token = jwtTokenService.generateAccessToken(userId, email, roles);
        
        // When
        Optional<JwtClaims> claimsOpt = jwtTokenService.validateAccessToken(token);
        
        // Then
        assertThat(claimsOpt).isPresent();
        JwtClaims claims = claimsOpt.get();
        assertThat(claims.userId()).isEqualTo(userId);
        assertThat(claims.email()).isEqualTo(email);
        assertThat(claims.roles()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(claims.tokenId()).isNotBlank();
    }
    
    @Test
    @DisplayName("Should include additional claims in token")
    void shouldIncludeAdditionalClaims() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Set<String> roles = Set.of("ROLE_USER");
        Map<String, Object> additionalClaims = Map.of(
            "subscriptionPlan", "premium",
            "tenantId", "tenant-123"
        );
        
        // When
        String token = jwtTokenService.generateAccessToken(userId, email, roles, additionalClaims);
        Optional<JwtClaims> claimsOpt = jwtTokenService.validateAccessToken(token);
        
        // Then
        assertThat(claimsOpt).isPresent();
        JwtClaims claims = claimsOpt.get();
        assertThat(claims.additionalClaims())
            .containsEntry("subscriptionPlan", "premium")
            .containsEntry("tenantId", "tenant-123");
    }
    
    @Test
    @DisplayName("Should return empty for invalid token")
    void shouldReturnEmptyForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When
        Optional<JwtClaims> claimsOpt = jwtTokenService.validateAccessToken(invalidToken);
        
        // Then
        assertThat(claimsOpt).isEmpty();
    }
    
    @Test
    @DisplayName("Should generate refresh token")
    void shouldGenerateRefreshToken() {
        // When
        String refreshToken1 = jwtTokenService.generateRefreshToken();
        String refreshToken2 = jwtTokenService.generateRefreshToken();
        
        // Then
        assertThat(refreshToken1).isNotBlank();
        assertThat(refreshToken2).isNotBlank();
        assertThat(refreshToken1).isNotEqualTo(refreshToken2); // Should be unique
    }
    
    @Test
    @DisplayName("Should extract user ID from token")
    void shouldExtractUserId() {
        // Given
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateAccessToken(
            userId, "test@example.com", Set.of("ROLE_USER")
        );
        
        // When
        Optional<UUID> extractedId = jwtTokenService.extractUserId(token);
        
        // Then
        assertThat(extractedId).contains(userId);
    }
    
    @Test
    @DisplayName("Should check token expiration correctly")
    void shouldCheckTokenExpiration() {
        // Given - Create a token with short expiration
        JwtProperties shortLivedProps = new JwtProperties();
        shortLivedProps.setSecret("test-secret-key-for-unit-tests-32-chars!");
        shortLivedProps.setAccessTokenExpiration(Duration.ofMinutes(15));
        shortLivedProps.setIssuer("test-issuer");
        
        JwtTokenService service = new JwtTokenService(shortLivedProps);
        String token = service.generateAccessToken(
            UUID.randomUUID(), "test@example.com", Set.of("ROLE_USER")
        );
        
        // When/Then - Token should not be expired immediately
        assertThat(service.isTokenExpired(token)).isFalse();
    }
    
    @Test
    @DisplayName("Should return correct expiration times")
    void shouldReturnCorrectExpirationTimes() {
        // When
        long accessExpSeconds = jwtTokenService.getAccessTokenExpirationSeconds();
        long refreshExpSeconds = jwtTokenService.getRefreshTokenExpirationSeconds();
        
        // Then
        assertThat(accessExpSeconds).isEqualTo(15 * 60); // 15 minutes
        assertThat(refreshExpSeconds).isEqualTo(7 * 24 * 60 * 60); // 7 days
    }
}

