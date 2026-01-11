package com.catalyst.shared.application.ports;

import com.catalyst.shared.domain.auth.JwtClaims;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Port interface for JWT token operations.
 * Application layer - defines the contract for token management.
 * 
 * <p>Implementations of this port handle:
 * <ul>
 *   <li>Access token generation (HS256 JWT)</li>
 *   <li>Token validation and claims extraction</li>
 *   <li>Refresh token generation (opaque tokens)</li>
 * </ul>
 */
public interface JwtTokenPort {
    
    /**
     * Generates an access token (JWT) for the specified user.
     *
     * @param userId the user's unique identifier
     * @param email the user's email address
     * @param roles the user's roles
     * @return the generated JWT access token
     */
    String generateAccessToken(UUID userId, String email, Set<String> roles);
    
    /**
     * Generates an access token with additional custom claims.
     *
     * @param userId the user's unique identifier
     * @param email the user's email address
     * @param roles the user's roles
     * @param additionalClaims custom claims to include in the token
     * @return the generated JWT access token
     */
    String generateAccessToken(
        UUID userId, 
        String email, 
        Set<String> roles,
        Map<String, Object> additionalClaims
    );
    
    /**
     * Generates an opaque refresh token.
     * The refresh token should be stored in the database for validation.
     *
     * @return the generated opaque refresh token
     */
    String generateRefreshToken();
    
    /**
     * Validates an access token and extracts its claims.
     *
     * @param token the JWT access token to validate
     * @return the claims if the token is valid, empty if invalid
     */
    Optional<JwtClaims> validateAccessToken(String token);
    
    /**
     * Extracts claims from a token without validation.
     * Use with caution - only for tokens that have already been validated.
     *
     * @param token the JWT token
     * @return the claims, or empty if the token is malformed
     */
    Optional<JwtClaims> extractClaims(String token);
    
    /**
     * Checks if a token is expired.
     *
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    boolean isTokenExpired(String token);
    
    /**
     * Extracts the user ID from a token.
     *
     * @param token the JWT token
     * @return the user ID, or empty if not present
     */
    Optional<UUID> extractUserId(String token);
    
    /**
     * Gets the configured access token expiration time in seconds.
     *
     * @return expiration time in seconds
     */
    long getAccessTokenExpirationSeconds();
    
    /**
     * Gets the configured refresh token expiration time in seconds.
     *
     * @return expiration time in seconds
     */
    long getRefreshTokenExpirationSeconds();
}

