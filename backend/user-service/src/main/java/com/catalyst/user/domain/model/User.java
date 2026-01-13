package com.catalyst.user.domain.model;

import com.catalyst.user.domain.event.PasswordResetCompleted;
import com.catalyst.user.domain.event.PasswordResetRequested;
import com.catalyst.user.domain.event.UserLoggedIn;
import com.catalyst.user.domain.event.UserRegistered;
import com.catalyst.user.domain.exception.InvalidPasswordException;
import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.HashedPassword;
import com.catalyst.user.domain.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * User aggregate root representing a system user.
 * 
 * <p>This is the main entity for user management, encapsulating:
 * <ul>
 *   <li>User identity (id, email, name)</li>
 *   <li>Authentication credentials (password for local auth)</li>
 *   <li>OAuth provider information (for social login)</li>
 *   <li>Authorization (role)</li>
 *   <li>Account status</li>
 * </ul>
 * 
 * <p>Domain events are registered on state changes and should be
 * published by the application service after persistence.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public class User {
    
    private UserId id;
    private Email email;
    private String name;
    private String imageUrl;
    private HashedPassword passwordHash;
    private AuthProvider provider;
    private String providerAccountId;
    private UserRole role;
    private boolean emailVerified;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    
    // Domain events to be published
    private final List<Object> domainEvents = new ArrayList<>();
    
    // Private constructor for factory methods
    private User() {
    }
    
    // ========================
    // Factory Methods
    // ========================
    
    /**
     * Creates a new user with local (email/password) authentication.
     * 
     * @param email the user's email address
     * @param name the user's display name
     * @param hashedPassword the BCrypt-hashed password
     * @return a new User instance
     */
    public static User registerWithCredentials(
            Email email,
            String name,
            HashedPassword hashedPassword) {
        
        User user = new User();
        user.id = UserId.generate();
        user.email = Objects.requireNonNull(email, "Email is required");
        user.name = validateName(name);
        user.passwordHash = Objects.requireNonNull(hashedPassword, "Password is required");
        user.provider = AuthProvider.LOCAL;
        user.providerAccountId = null;
        user.role = UserRole.USER;
        user.emailVerified = false;
        user.active = true;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = user.createdAt;
        
        user.registerEvent(new UserRegistered(
            user.id,
            user.email,
            user.name,
            user.provider,
            user.role
        ));
        
        return user;
    }
    
    /**
     * Creates or updates a user from social OAuth provider.
     * 
     * @param email the user's email from the OAuth provider
     * @param name the user's name from the OAuth provider
     * @param imageUrl the user's profile image URL
     * @param provider the OAuth provider (GOOGLE, GITHUB)
     * @param providerAccountId the unique ID from the provider
     * @return a new User instance
     */
    public static User registerWithOAuth(
            Email email,
            String name,
            String imageUrl,
            AuthProvider provider,
            String providerAccountId) {
        
        if (!provider.isSocial()) {
            throw new IllegalArgumentException("Provider must be a social OAuth provider");
        }
        
        User user = new User();
        user.id = UserId.generate();
        user.email = Objects.requireNonNull(email, "Email is required");
        user.name = name;
        user.imageUrl = imageUrl;
        user.passwordHash = null; // Social users don't have passwords
        user.provider = provider;
        user.providerAccountId = Objects.requireNonNull(providerAccountId, "Provider account ID is required");
        user.role = UserRole.USER;
        user.emailVerified = true; // OAuth providers verify email
        user.active = true;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = user.createdAt;
        
        user.registerEvent(new UserRegistered(
            user.id,
            user.email,
            user.name,
            user.provider,
            user.role
        ));
        
        return user;
    }
    
    /**
     * Reconstitutes a User from persistence.
     * Used by repository adapters to create domain objects from JPA entities.
     */
    public static User reconstitute(
            UserId id,
            Email email,
            String name,
            String imageUrl,
            HashedPassword passwordHash,
            AuthProvider provider,
            String providerAccountId,
            UserRole role,
            boolean emailVerified,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime lastLoginAt) {
        
        User user = new User();
        user.id = id;
        user.email = email;
        user.name = name;
        user.imageUrl = imageUrl;
        user.passwordHash = passwordHash;
        user.provider = provider;
        user.providerAccountId = providerAccountId;
        user.role = role;
        user.emailVerified = emailVerified;
        user.active = active;
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        user.lastLoginAt = lastLoginAt;
        return user;
    }
    
    // ========================
    // Domain Operations
    // ========================
    
    /**
     * Records a successful login and updates last login timestamp.
     * 
     * @param ipAddress the IP address of the login request
     */
    public void recordLogin(String ipAddress) {
        if (!active) {
            throw new IllegalStateException("Cannot login to inactive account");
        }
        
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        registerEvent(new UserLoggedIn(id, email, provider, ipAddress));
    }
    
    /**
     * Updates the user's profile information.
     * 
     * @param name the new display name
     * @param imageUrl the new profile image URL (optional)
     */
    public void updateProfile(String name, String imageUrl) {
        this.name = validateName(name);
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Updates user from OAuth provider sync.
     * Only updates fields that come from the provider.
     * 
     * @param name the name from OAuth
     * @param imageUrl the image URL from OAuth
     */
    public void syncFromOAuth(String name, String imageUrl) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (imageUrl != null && !imageUrl.isBlank()) {
            this.imageUrl = imageUrl;
        }
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Changes the user's password.
     * 
     * @param newHashedPassword the new BCrypt-hashed password
     * @throws InvalidPasswordException if user uses OAuth authentication
     */
    public void changePassword(HashedPassword newHashedPassword) {
        if (provider.isSocial()) {
            throw new InvalidPasswordException("Social login users cannot change password");
        }
        
        this.passwordHash = Objects.requireNonNull(newHashedPassword, "New password is required");
        this.updatedAt = LocalDateTime.now();
        
        registerEvent(new PasswordResetCompleted(id, email));
    }
    
    /**
     * Requests a password reset, generating a token.
     * 
     * @param token the password reset token
     * @param expiresAt when the token expires
     */
    public void requestPasswordReset(String token, LocalDateTime expiresAt) {
        if (provider.isSocial()) {
            throw new InvalidPasswordException("Social login users cannot reset password");
        }
        
        registerEvent(new PasswordResetRequested(id, email, token, expiresAt));
    }
    
    /**
     * Verifies the user's email address.
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Deactivates the user account.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Reactivates a deactivated user account.
     */
    public void reactivate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Promotes user to admin role.
     */
    public void promoteToAdmin() {
        this.role = UserRole.ADMIN;
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========================
    // Query Methods
    // ========================
    
    /**
     * Checks if user can authenticate with password.
     * 
     * @return true if user has local authentication
     */
    public boolean canAuthenticateWithPassword() {
        return provider == AuthProvider.LOCAL && passwordHash != null;
    }
    
    /**
     * Checks if user has admin privileges.
     * 
     * @return true if user is admin
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
    
    // ========================
    // Domain Events
    // ========================
    
    private void registerEvent(Object event) {
        domainEvents.add(event);
    }
    
    /**
     * Returns and clears all pending domain events.
     * 
     * @return list of domain events to publish
     */
    public List<Object> getDomainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }
    
    /**
     * Clears all pending domain events without returning them.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }
    
    // ========================
    // Validation Helpers
    // ========================
    
    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 255) {
            throw new IllegalArgumentException("Name cannot exceed 255 characters");
        }
        return trimmed;
    }
    
    // ========================
    // Getters
    // ========================
    
    public UserId getId() {
        return id;
    }
    
    public Email getEmail() {
        return email;
    }
    
    public String getName() {
        return name;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public HashedPassword getPasswordHash() {
        return passwordHash;
    }
    
    public AuthProvider getProvider() {
        return provider;
    }
    
    public String getProviderAccountId() {
        return providerAccountId;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public boolean isEmailVerified() {
        return emailVerified;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    // ========================
    // Object Methods
    // ========================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email=" + email +
                ", name='" + name + '\'' +
                ", provider=" + provider +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}

