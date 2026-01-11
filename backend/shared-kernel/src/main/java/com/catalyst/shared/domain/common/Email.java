package com.catalyst.shared.domain.common;

import com.catalyst.shared.domain.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Value object representing a validated email address.
 */
public record Email(String value) implements ValueObject {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    public Email {
        if (value == null || value.isBlank()) {
            throw ValidationException.required("email");
        }
        value = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw ValidationException.invalidEmail(value);
        }
    }
    
    /**
     * Creates an Email from a string.
     */
    public static Email of(String value) {
        return new Email(value);
    }
    
    /**
     * Gets the domain part of the email.
     */
    public String domain() {
        return value.substring(value.indexOf('@') + 1);
    }
    
    /**
     * Gets the local part (before @) of the email.
     */
    public String localPart() {
        return value.substring(0, value.indexOf('@'));
    }
    
    @Override
    public String toString() {
        return value;
    }
}

