package com.catalyst.user.domain.model;

/**
 * Enumeration representing user roles in the system.
 * 
 * <p>Roles define the access level and permissions for users:
 * <ul>
 *   <li>{@link #USER} - Standard user with basic access</li>
 *   <li>{@link #ADMIN} - Administrator with full system access</li>
 * </ul>
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public enum UserRole {
    
    /**
     * Standard user role with basic access permissions.
     * Can manage own profile, subscriptions, and settings.
     */
    USER("USER", "Standard User"),
    
    /**
     * Administrator role with full system access.
     * Can manage all users, subscriptions, and system settings.
     */
    ADMIN("ADMIN", "Administrator");
    
    private final String code;
    private final String displayName;
    
    UserRole(String code, String displayName) {
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
     * Returns the Spring Security authority string for this role.
     * 
     * @return authority string prefixed with "ROLE_"
     */
    public String getAuthority() {
        return "ROLE_" + code;
    }
    
    /**
     * Checks if this role has admin privileges.
     * 
     * @return true if this is the ADMIN role
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
    
    /**
     * Parses a role from its code string.
     * 
     * @param code the role code
     * @return the corresponding UserRole
     * @throws IllegalArgumentException if the code is not valid
     */
    public static UserRole fromCode(String code) {
        if (code == null) {
            return USER; // Default role
        }
        for (UserRole role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + code);
    }
}

