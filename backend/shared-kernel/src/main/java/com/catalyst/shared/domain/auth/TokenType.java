package com.catalyst.shared.domain.auth;

/**
 * Enumeration of token types supported by the system.
 */
public enum TokenType {
    
    /**
     * Access token - short-lived JWT for API authentication.
     * Typically expires in 15 minutes.
     */
    ACCESS("access"),
    
    /**
     * Refresh token - long-lived opaque token for obtaining new access tokens.
     * Typically expires in 7 days. Stored in database.
     */
    REFRESH("refresh"),
    
    /**
     * Password reset token - single-use token for password recovery.
     * Typically expires in 1 hour.
     */
    PASSWORD_RESET("password_reset"),
    
    /**
     * Email verification token - single-use token for email confirmation.
     * Typically expires in 24 hours.
     */
    EMAIL_VERIFICATION("email_verification");
    
    private final String value;
    
    TokenType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static TokenType fromValue(String value) {
        for (TokenType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown token type: " + value);
    }
}

