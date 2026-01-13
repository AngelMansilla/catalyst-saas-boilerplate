package com.catalyst.user.application.ports.input;

import com.catalyst.user.application.dto.NewPasswordRequest;

/**
 * Use case for resetting a password using a reset token.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface ResetPasswordUseCase {
    
    /**
     * Resets the password using the provided token.
     * 
     * @param request containing the token and new password
     * @throws com.catalyst.user.domain.exception.InvalidResetTokenException if token invalid
     * @throws com.catalyst.user.domain.exception.InvalidPasswordException if password weak
     */
    void resetPassword(NewPasswordRequest request);
}

