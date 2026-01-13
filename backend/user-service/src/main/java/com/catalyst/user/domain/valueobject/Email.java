package com.catalyst.user.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a validated email address.
 * 
 * <p>This immutable value object ensures that email addresses are always valid
 * and normalized (lowercase, trimmed).
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public final class Email {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final int MAX_LENGTH = 255;
    
    private final String value;
    
    private Email(String value) {
        this.value = value;
    }
    
    /**
     * Creates an Email from a string value.
     * 
     * @param value the email address string
     * @return a validated Email instance
     * @throws IllegalArgumentException if the email is null, blank, or invalid
     */
    public static Email of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        
        String normalized = value.trim().toLowerCase();
        
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Email cannot exceed " + MAX_LENGTH + " characters");
        }
        
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        
        return new Email(normalized);
    }
    
    /**
     * Creates an Email from a string value, or null if invalid.
     * 
     * @param value the email address string
     * @return a validated Email instance, or null if invalid
     */
    public static Email ofNullable(String value) {
        try {
            return of(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Returns the email address value.
     * 
     * @return the normalized email address
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Returns the domain part of the email address.
     * 
     * @return the domain (part after @)
     */
    public String getDomain() {
        int atIndex = value.indexOf('@');
        return value.substring(atIndex + 1);
    }
    
    /**
     * Returns the local part of the email address.
     * 
     * @return the local part (part before @)
     */
    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return value.substring(0, atIndex);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}

