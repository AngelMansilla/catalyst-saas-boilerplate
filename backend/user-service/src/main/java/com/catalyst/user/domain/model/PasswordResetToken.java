package com.catalyst.user.domain.model;

import com.catalyst.user.domain.valueobject.UserId;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a password reset token.
 * 
 * <p>This entity manages the lifecycle of a password reset request:
 * <ul>
 *   <li>Token generation with secure random bytes</li>
 *   <li>Expiration management</li>
 *   <li>Single-use enforcement</li>
 * </ul>
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public class PasswordResetToken {
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 48; // 64 chars in Base64
    
    private UUID id;
    private UserId userId;
    private String token;
    private LocalDateTime expiresAt;
    private boolean used;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
    private String ipAddress;
    
    // Private constructor for factory methods
    private PasswordResetToken() {
    }
    
    // ========================
    // Factory Methods
    // ========================
    
    /**
     * Creates a new password reset token for a user.
     * 
     * @param userId the user requesting password reset
     * @param expirationHours hours until token expires
     * @param ipAddress the IP address of the request
     * @return a new PasswordResetToken instance
     */
    public static PasswordResetToken create(UserId userId, int expirationHours, String ipAddress) {
        PasswordResetToken token = new PasswordResetToken();
        token.id = UUID.randomUUID();
        token.userId = Objects.requireNonNull(userId, "User ID is required");
        token.token = generateSecureToken();
        token.expiresAt = LocalDateTime.now().plusHours(expirationHours);
        token.used = false;
        token.usedAt = null;
        token.createdAt = LocalDateTime.now();
        token.ipAddress = ipAddress;
        return token;
    }
    
    /**
     * Reconstitutes a PasswordResetToken from persistence.
     */
    public static PasswordResetToken reconstitute(
            UUID id,
            UserId userId,
            String token,
            LocalDateTime expiresAt,
            boolean used,
            LocalDateTime usedAt,
            LocalDateTime createdAt,
            String ipAddress) {
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.id = id;
        resetToken.userId = userId;
        resetToken.token = token;
        resetToken.expiresAt = expiresAt;
        resetToken.used = used;
        resetToken.usedAt = usedAt;
        resetToken.createdAt = createdAt;
        resetToken.ipAddress = ipAddress;
        return resetToken;
    }
    
    // ========================
    // Domain Operations
    // ========================
    
    /**
     * Validates and uses this token.
     * 
     * @throws IllegalStateException if token is invalid, expired, or already used
     */
    public void use() {
        if (used) {
            throw new IllegalStateException("Token has already been used");
        }
        
        if (isExpired()) {
            throw new IllegalStateException("Token has expired");
        }
        
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if the token has expired.
     * 
     * @return true if current time is past expiration
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Checks if the token is still valid (not used and not expired).
     * 
     * @return true if token can be used
     */
    public boolean isValid() {
        return !used && !isExpired();
    }
    
    /**
     * Validates the provided token string against this token.
     * 
     * @param tokenToValidate the token string to validate
     * @return true if tokens match
     */
    public boolean matches(String tokenToValidate) {
        return token.equals(tokenToValidate);
    }
    
    // ========================
    // Token Generation
    // ========================
    
    private static String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    // ========================
    // Getters
    // ========================
    
    public UUID getId() {
        return id;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public String getToken() {
        return token;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public boolean isUsed() {
        return used;
    }
    
    public LocalDateTime getUsedAt() {
        return usedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    // ========================
    // Object Methods
    // ========================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordResetToken that = (PasswordResetToken) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id=" + id +
                ", userId=" + userId +
                ", expiresAt=" + expiresAt +
                ", used=" + used +
                '}';
    }
}

