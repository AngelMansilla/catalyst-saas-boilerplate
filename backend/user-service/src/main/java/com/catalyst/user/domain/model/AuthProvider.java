package com.catalyst.user.domain.model;

/**
 * Enumeration representing authentication providers.
 * 
 * <p>Defines the different ways users can authenticate:
 * <ul>
 *   <li>{@link #LOCAL} - Email and password authentication</li>
 *   <li>{@link #GOOGLE} - Google OAuth authentication</li>
 *   <li>{@link #GITHUB} - GitHub OAuth authentication</li>
 * </ul>
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public enum AuthProvider {
    
    /**
     * Local authentication using email and password.
     */
    LOCAL("local", "Email/Password"),
    
    /**
     * Google OAuth 2.0 authentication.
     */
    GOOGLE("google", "Google"),
    
    /**
     * GitHub OAuth authentication.
     */
    GITHUB("github", "GitHub");
    
    private final String code;
    private final String displayName;
    
    AuthProvider(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Checks if this provider is a social/OAuth provider.
     * 
     * @return true if this is GOOGLE or GITHUB
     */
    public boolean isSocial() {
        return this == GOOGLE || this == GITHUB;
    }
    
    /**
     * Checks if this provider requires a password.
     * 
     * @return true if this is LOCAL
     */
    public boolean requiresPassword() {
        return this == LOCAL;
    }
    
    /**
     * Parses a provider from its code string.
     * 
     * @param code the provider code (case-insensitive)
     * @return the corresponding AuthProvider
     * @throws IllegalArgumentException if the code is not valid
     */
    public static AuthProvider fromCode(String code) {
        if (code == null || code.isBlank()) {
            return LOCAL; // Default provider
        }
        for (AuthProvider provider : values()) {
            if (provider.code.equalsIgnoreCase(code) || provider.name().equalsIgnoreCase(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown auth provider: " + code);
    }
}

