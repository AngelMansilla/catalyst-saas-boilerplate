package com.catalyst.shared.infrastructure.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Configuration properties for cryptographic operations.
 * 
 * <p>Configure in application.yml:
 * <pre>
 * catalyst:
 *   security:
 *     crypto:
 *       encryption-key: your-32-character-secret-key-here
 *       bcrypt-strength: 12
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "catalyst.security.crypto")
public class CryptoProperties {
    
    /**
     * AES encryption key (must be 32 characters for AES-256).
     */
    @NotBlank(message = "Encryption key is required")
    @Size(min = 32, max = 32, message = "Encryption key must be exactly 32 characters")
    private String encryptionKey;
    
    /**
     * BCrypt strength factor (4-31).
     * Higher values are more secure but slower.
     * Default: 12 (good balance of security and performance)
     */
    @Min(value = 4, message = "BCrypt strength must be at least 4")
    private int bcryptStrength = 12;
    
    /**
     * Salt length for encryption (in bytes).
     */
    @Min(value = 8, message = "Salt length must be at least 8 bytes")
    private int saltLength = 16;
    
    // Getters and Setters
    
    public String getEncryptionKey() {
        return encryptionKey;
    }
    
    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
    
    public int getBcryptStrength() {
        return bcryptStrength;
    }
    
    public void setBcryptStrength(int bcryptStrength) {
        this.bcryptStrength = bcryptStrength;
    }
    
    public int getSaltLength() {
        return saltLength;
    }
    
    public void setSaltLength(int saltLength) {
        this.saltLength = saltLength;
    }
}

