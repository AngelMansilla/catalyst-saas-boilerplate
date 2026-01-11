package com.catalyst.shared.application.ports;

/**
 * Port interface for encryption operations.
 * Application layer - defines the contract for cryptographic operations.
 */
public interface EncryptionPort {
    
    /**
     * Encrypts a plaintext string using AES-256.
     *
     * @param plaintext the text to encrypt
     * @return the encrypted text (Base64 encoded)
     */
    String encrypt(String plaintext);
    
    /**
     * Decrypts an encrypted string.
     *
     * @param ciphertext the encrypted text (Base64 encoded)
     * @return the decrypted plaintext
     */
    String decrypt(String ciphertext);
    
    /**
     * Hashes a password using BCrypt.
     *
     * @param rawPassword the raw password
     * @return the hashed password
     */
    String hashPassword(String rawPassword);
    
    /**
     * Verifies a password against its hash.
     *
     * @param rawPassword the raw password to verify
     * @param hashedPassword the stored password hash
     * @return true if the password matches, false otherwise
     */
    boolean verifyPassword(String rawPassword, String hashedPassword);
    
    /**
     * Generates a secure random string.
     *
     * @param length the length of the string in bytes
     * @return a secure random string (Base64 encoded)
     */
    String generateSecureRandom(int length);
    
    /**
     * Generates a secure random token suitable for URLs.
     *
     * @param length the length of the token in bytes
     * @return a URL-safe token
     */
    String generateUrlSafeToken(int length);
    
    /**
     * Computes a SHA-256 hash of the input.
     *
     * @param input the input string
     * @return the hexadecimal hash
     */
    String sha256Hash(String input);
}

