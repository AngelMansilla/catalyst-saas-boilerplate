package com.catalyst.user.application.ports.output;

import com.catalyst.user.domain.model.PasswordResetToken;
import com.catalyst.user.domain.valueobject.UserId;

import java.util.Optional;

/**
 * Output port for password reset token persistence.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface PasswordResetTokenRepository {
    
    /**
     * Saves a password reset token.
     * 
     * @param token the token to save
     * @return the saved token
     */
    PasswordResetToken save(PasswordResetToken token);
    
    /**
     * Finds a token by its value.
     * 
     * @param token the token string
     * @return optional containing token if found
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Finds the latest valid token for a user.
     * 
     * @param userId the user ID
     * @return optional containing token if found
     */
    Optional<PasswordResetToken> findLatestValidByUserId(UserId userId);
    
    /**
     * Invalidates all tokens for a user.
     * Used when password is successfully reset.
     * 
     * @param userId the user ID
     */
    void invalidateAllForUser(UserId userId);
    
    /**
     * Deletes expired and used tokens.
     * Should be called periodically by a scheduled task.
     * 
     * @return number of tokens deleted
     */
    int deleteExpiredAndUsed();
}

