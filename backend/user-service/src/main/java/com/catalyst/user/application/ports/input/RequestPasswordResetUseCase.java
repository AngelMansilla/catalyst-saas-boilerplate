package com.catalyst.user.application.ports.input;

import com.catalyst.user.application.dto.PasswordResetRequest;

/**
 * Use case for requesting a password reset.
 * Sends a password reset email with a secure token.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface RequestPasswordResetUseCase {
    
    /**
     * Requests a password reset for the given email.
     * 
     * <p>Note: This method always succeeds silently even if email doesn't exist.
     * This is to prevent email enumeration attacks.
     * 
     * @param request containing the email address
     * @param ipAddress the IP address of the request
     */
    void requestReset(PasswordResetRequest request, String ipAddress);
}

