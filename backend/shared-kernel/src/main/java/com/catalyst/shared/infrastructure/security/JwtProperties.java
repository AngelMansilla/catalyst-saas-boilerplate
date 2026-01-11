package com.catalyst.shared.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;

/**
 * Configuration properties for JWT token management.
 * 
 * <p>Configure in application.yml:
 * <pre>
 * catalyst:
 *   security:
 *     jwt:
 *       secret: your-256-bit-secret-key-here
 *       access-token-expiration: 15m
 *       refresh-token-expiration: 7d
 *       issuer: catalyst-api
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "catalyst.security.jwt")
public class JwtProperties {
    
    /**
     * Secret key for signing JWTs (HS256).
     * Must be at least 256 bits (32 characters) for security.
     */
    @NotBlank(message = "JWT secret is required")
    @Size(min = 32, message = "JWT secret must be at least 32 characters (256 bits)")
    private String secret;
    
    /**
     * Access token expiration duration.
     * Default: 15 minutes
     */
    private Duration accessTokenExpiration = Duration.ofMinutes(15);
    
    /**
     * Refresh token expiration duration.
     * Default: 7 days
     */
    private Duration refreshTokenExpiration = Duration.ofDays(7);
    
    /**
     * JWT issuer claim value.
     * Default: catalyst-api
     */
    private String issuer = "catalyst-api";
    
    /**
     * JWT audience claim value.
     * Default: catalyst-client
     */
    private String audience = "catalyst-client";
    
    /**
     * Length of generated refresh tokens in bytes.
     * Default: 32 bytes (256 bits)
     */
    @Min(value = 32, message = "Refresh token length must be at least 32 bytes")
    private int refreshTokenLength = 32;
    
    // Getters and Setters
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public Duration getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
    
    public void setAccessTokenExpiration(Duration accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }
    
    public Duration getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
    
    public void setRefreshTokenExpiration(Duration refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public String getAudience() {
        return audience;
    }
    
    public void setAudience(String audience) {
        this.audience = audience;
    }
    
    public int getRefreshTokenLength() {
        return refreshTokenLength;
    }
    
    public void setRefreshTokenLength(int refreshTokenLength) {
        this.refreshTokenLength = refreshTokenLength;
    }
    
    /**
     * Returns access token expiration in seconds.
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration.toSeconds();
    }
    
    /**
     * Returns refresh token expiration in seconds.
     */
    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpiration.toSeconds();
    }
}

