package com.catalyst.user.application.ports.output;

import com.catalyst.user.domain.model.User;
import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Output port for user persistence operations.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface UserRepository {
    
    /**
     * Saves a user (create or update).
     * 
     * @param user the user to save
     * @return the saved user
     */
    User save(User user);
    
    /**
     * Finds a user by ID.
     * 
     * @param userId the user ID
     * @return optional containing user if found
     */
    Optional<User> findById(UserId userId);
    
    /**
     * Finds a user by email.
     * 
     * @param email the email address
     * @return optional containing user if found
     */
    Optional<User> findByEmail(Email email);
    
    /**
     * Finds a user by OAuth provider and account ID.
     * 
     * @param provider the OAuth provider (e.g., "google", "github")
     * @param providerAccountId the account ID from the provider
     * @return optional containing user if found
     */
    Optional<User> findByProviderAndProviderAccountId(String provider, String providerAccountId);
    
    /**
     * Checks if an email is already registered.
     * 
     * @param email the email to check
     * @return true if email exists
     */
    boolean existsByEmail(Email email);
    
    /**
     * Lists all users with pagination.
     * 
     * @param page page number (0-based)
     * @param size page size
     * @return list of users
     */
    List<User> findAll(int page, int size);
    
    /**
     * Counts total number of users.
     * 
     * @return total user count
     */
    long count();
    
    /**
     * Deletes a user.
     * 
     * @param user the user to delete
     */
    void delete(User user);
}

