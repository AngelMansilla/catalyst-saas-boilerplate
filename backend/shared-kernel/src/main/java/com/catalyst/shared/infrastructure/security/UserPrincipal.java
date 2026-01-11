package com.catalyst.shared.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of UserDetails that holds authenticated user information.
 * Used as the principal in Spring Security's SecurityContext.
 */
public record UserPrincipal(
    UUID id,
    String email,
    Set<String> roles
) implements UserDetails {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(SimpleGrantedAuthority::new)
            .toList();
    }
    
    @Override
    public String getPassword() {
        // Password is not stored in the principal
        return null;
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    /**
     * Checks if the user has a specific role.
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
    
    /**
     * Checks if the user has any of the specified roles.
     */
    public boolean hasAnyRole(String... rolesToCheck) {
        for (String role : rolesToCheck) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Creates a UserPrincipal with a single role.
     */
    public static UserPrincipal withRole(UUID id, String email, String role) {
        return new UserPrincipal(id, email, Set.of(role));
    }
    
    /**
     * Creates a UserPrincipal with default USER role.
     */
    public static UserPrincipal defaultUser(UUID id, String email) {
        return new UserPrincipal(id, email, Set.of("ROLE_USER"));
    }
}

