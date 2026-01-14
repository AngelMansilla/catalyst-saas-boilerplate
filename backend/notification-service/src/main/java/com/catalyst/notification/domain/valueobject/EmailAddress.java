package com.catalyst.notification.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing an email address with validation.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record EmailAddress(String value) {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    public EmailAddress {
        Objects.requireNonNull(value, "Email address cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be blank");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email address format: " + value);
        }
    }
    
    @Override
    public String toString() {
        return value;
    }
}

