package com.catalyst.user.application.ports.input;

import com.catalyst.user.application.dto.SyncUserRequest;
import com.catalyst.user.application.dto.UserResponse;

/**
 * Use case for synchronizing users from OAuth providers.
 * Called by NextAuth after successful OAuth authentication.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface SyncSocialUserUseCase {
    
    /**
     * Syncs a user from an OAuth provider.
     * Creates a new user if they don't exist, or updates existing user.
     * 
     * @param request the OAuth user data
     * @return the synced user information
     */
    UserResponse sync(SyncUserRequest request);
}

