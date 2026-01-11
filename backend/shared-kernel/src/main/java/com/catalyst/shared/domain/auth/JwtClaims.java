package com.catalyst.shared.domain.auth;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable value object representing JWT claims.
 * Domain layer - no framework dependencies.
 */
public record JwtClaims(
    UUID userId,
    String email,
    Set<String> roles,
    Instant issuedAt,
    Instant expiresAt,
    String tokenId,
    Map<String, Object> additionalClaims
) {
    
    public JwtClaims {
        // Defensive copies for immutability
        roles = roles != null ? Set.copyOf(roles) : Set.of();
        additionalClaims = additionalClaims != null 
            ? Map.copyOf(additionalClaims) 
            : Map.of();
    }
    
    /**
     * Creates a builder for JwtClaims.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Checks if the token has expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
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
     * Builder for JwtClaims.
     */
    public static class Builder {
        private UUID userId;
        private String email;
        private Set<String> roles = Set.of();
        private Instant issuedAt;
        private Instant expiresAt;
        private String tokenId;
        private Map<String, Object> additionalClaims = Map.of();
        
        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }
        
        public Builder issuedAt(Instant issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }
        
        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }
        
        public Builder tokenId(String tokenId) {
            this.tokenId = tokenId;
            return this;
        }
        
        public Builder additionalClaims(Map<String, Object> additionalClaims) {
            this.additionalClaims = additionalClaims;
            return this;
        }
        
        public JwtClaims build() {
            return new JwtClaims(
                userId, email, roles, issuedAt, 
                expiresAt, tokenId, additionalClaims
            );
        }
    }
}

