package com.catalyst.user.application.ports.input;

import com.catalyst.user.application.dto.UpdateProfileRequest;
import com.catalyst.user.application.dto.UserResponse;
import com.catalyst.user.domain.valueobject.UserId;

/**
 * Use case for updating user profile.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface UpdateUserProfileUseCase {
    
    /**
     * Updates the user's profile information.
     * 
     * @param userId the user ID
     * @param request the profile update data
     * @return the updated user information
     * @throws com.catalyst.user.domain.exception.UserNotFoundException if not found
     */
    UserResponse updateProfile(UserId userId, UpdateProfileRequest request);
}

