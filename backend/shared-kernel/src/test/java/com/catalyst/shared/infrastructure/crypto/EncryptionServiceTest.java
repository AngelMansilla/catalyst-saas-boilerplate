package com.catalyst.shared.infrastructure.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for EncryptionService.
 */
class EncryptionServiceTest {
    
    private EncryptionService encryptionService;
    
    @BeforeEach
    void setUp() {
        CryptoProperties properties = new CryptoProperties();
        properties.setEncryptionKey("12345678901234567890123456789012"); // 32 chars
        properties.setBcryptStrength(4); // Low for tests
        
        encryptionService = new EncryptionService(properties);
    }
    
    @Test
    @DisplayName("Should encrypt and decrypt data correctly")
    void shouldEncryptAndDecrypt() {
        // Given
        String plaintext = "Hello, World! This is sensitive data.";
        
        // When
        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);
        
        // Then
        assertThat(encrypted).isNotEqualTo(plaintext);
        assertThat(decrypted).isEqualTo(plaintext);
    }
    
    @Test
    @DisplayName("Should produce different ciphertext for same plaintext")
    void shouldProduceDifferentCiphertext() {
        // Given
        String plaintext = "Same text";
        
        // When
        String encrypted1 = encryptionService.encrypt(plaintext);
        String encrypted2 = encryptionService.encrypt(plaintext);
        
        // Then - IV is random, so ciphertext should differ
        assertThat(encrypted1).isNotEqualTo(encrypted2);
        
        // But both should decrypt to same value
        assertThat(encryptionService.decrypt(encrypted1)).isEqualTo(plaintext);
        assertThat(encryptionService.decrypt(encrypted2)).isEqualTo(plaintext);
    }
    
    @Test
    @DisplayName("Should hash password correctly")
    void shouldHashPassword() {
        // Given
        String password = "MySecurePassword123!";
        
        // When
        String hash = encryptionService.hashPassword(password);
        
        // Then
        assertThat(hash).isNotEqualTo(password);
        assertThat(hash).startsWith("$2"); // BCrypt prefix
    }
    
    @Test
    @DisplayName("Should verify password correctly")
    void shouldVerifyPassword() {
        // Given
        String password = "MySecurePassword123!";
        String hash = encryptionService.hashPassword(password);
        
        // When/Then
        assertThat(encryptionService.verifyPassword(password, hash)).isTrue();
        assertThat(encryptionService.verifyPassword("WrongPassword", hash)).isFalse();
    }
    
    @Test
    @DisplayName("Should generate secure random strings")
    void shouldGenerateSecureRandom() {
        // When
        String random1 = encryptionService.generateSecureRandom(32);
        String random2 = encryptionService.generateSecureRandom(32);
        
        // Then
        assertThat(random1).isNotBlank();
        assertThat(random2).isNotBlank();
        assertThat(random1).isNotEqualTo(random2);
    }
    
    @Test
    @DisplayName("Should generate URL-safe tokens")
    void shouldGenerateUrlSafeToken() {
        // When
        String token = encryptionService.generateUrlSafeToken(32);
        
        // Then
        assertThat(token).isNotBlank();
        assertThat(token).matches("[A-Za-z0-9_-]+"); // URL-safe characters only
    }
    
    @Test
    @DisplayName("Should compute SHA-256 hash")
    void shouldComputeSha256Hash() {
        // Given
        String input = "Hello, World!";
        
        // When
        String hash = encryptionService.sha256Hash(input);
        
        // Then
        assertThat(hash).hasSize(64); // SHA-256 = 256 bits = 64 hex chars
        assertThat(hash).matches("[a-f0-9]+");
        
        // Same input should produce same hash
        assertThat(encryptionService.sha256Hash(input)).isEqualTo(hash);
    }
    
    @Test
    @DisplayName("Should handle empty string encryption")
    void shouldHandleEmptyStringEncryption() {
        // Given
        String plaintext = "";
        
        // When
        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);
        
        // Then
        assertThat(decrypted).isEmpty();
    }
    
    @Test
    @DisplayName("Should handle unicode characters")
    void shouldHandleUnicodeCharacters() {
        // Given
        String plaintext = "Hello 世界! Привет мир! 🎉";
        
        // When
        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);
        
        // Then
        assertThat(decrypted).isEqualTo(plaintext);
    }
}

