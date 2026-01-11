package com.catalyst.shared.domain.auth;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for successful login.
 */
public record LoginResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    Instant expiresAt,
    UserInfo user
) {
    
    private static final String BEARER_TYPE = "Bearer";
    
    /**
     * Creates a login response.
     */
    public static LoginResponse of(
            String accessToken,
            String refreshToken,
            long expiresInSeconds,
            UserInfo user) {
        return new LoginResponse(
            accessToken,
            refreshToken,
            BEARER_TYPE,
            expiresInSeconds,
            Instant.now().plusSeconds(expiresInSeconds),
            user
        );
    }
    
    /**
     * User information included in login response.
     */
    public record UserInfo(
        UUID id,
        String email,
        String name,
        Set<String> roles,
        String avatarUrl,
        String subscriptionPlan
    ) {
        
        public static UserInfo of(UUID id, String email, String name, Set<String> roles) {
            return new UserInfo(id, email, name, roles, null, null);
        }
        
        public UserInfo withAvatar(String avatarUrl) {
            return new UserInfo(id, email, name, roles, avatarUrl, subscriptionPlan);
        }
        
        public UserInfo withSubscription(String plan) {
            return new UserInfo(id, email, name, roles, avatarUrl, plan);
        }
    }
}

