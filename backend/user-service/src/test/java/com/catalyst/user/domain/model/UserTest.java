package com.catalyst.user.domain.model;

import com.catalyst.user.domain.event.PasswordResetCompleted;
import com.catalyst.user.domain.event.PasswordResetRequested;
import com.catalyst.user.domain.event.UserLoggedIn;
import com.catalyst.user.domain.event.UserRegistered;
import com.catalyst.user.domain.exception.InvalidPasswordException;
import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.HashedPassword;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User aggregate root.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@DisplayName("User Aggregate")
class UserTest {
    
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_NAME = "John Doe";
    private static final String VALID_PASSWORD_HASH = "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.W7j7W3L4X8OM2e";
    
    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {
        
        @Test
        @DisplayName("Should create user with local credentials")
        void shouldCreateUserWithLocalCredentials() {
            // Given
            Email email = Email.of(VALID_EMAIL);
            HashedPassword password = HashedPassword.fromHash(VALID_PASSWORD_HASH);
            
            // When
            User user = User.registerWithCredentials(email, VALID_NAME, password);
            
            // Then
            assertNotNull(user.getId());
            assertEquals(email, user.getEmail());
            assertEquals(VALID_NAME, user.getName());
            assertEquals(password, user.getPasswordHash());
            assertEquals(AuthProvider.LOCAL, user.getProvider());
            assertNull(user.getProviderAccountId());
            assertEquals(UserRole.USER, user.getRole());
            assertFalse(user.isEmailVerified());
            assertTrue(user.isActive());
            assertNotNull(user.getCreatedAt());
        }
        
        @Test
        @DisplayName("Should emit UserRegistered event on local registration")
        void shouldEmitUserRegisteredEventOnLocalRegistration() {
            // Given
            Email email = Email.of(VALID_EMAIL);
            HashedPassword password = HashedPassword.fromHash(VALID_PASSWORD_HASH);
            
            // When
            User user = User.registerWithCredentials(email, VALID_NAME, password);
            
            // Then
            var events = user.getDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(UserRegistered.class, events.get(0));
            
            UserRegistered event = (UserRegistered) events.get(0);
            assertEquals(user.getId(), event.userId());
            assertEquals(email, event.email());
            assertEquals(AuthProvider.LOCAL, event.provider());
        }
        
        @Test
        @DisplayName("Should create user with OAuth provider")
        void shouldCreateUserWithOAuthProvider() {
            // Given
            Email email = Email.of(VALID_EMAIL);
            String imageUrl = "https://example.com/avatar.jpg";
            String providerAccountId = "google-12345";
            
            // When
            User user = User.registerWithOAuth(
                email, VALID_NAME, imageUrl, AuthProvider.GOOGLE, providerAccountId
            );
            
            // Then
            assertNotNull(user.getId());
            assertEquals(email, user.getEmail());
            assertEquals(VALID_NAME, user.getName());
            assertEquals(imageUrl, user.getImageUrl());
            assertNull(user.getPasswordHash()); // OAuth users have no password
            assertEquals(AuthProvider.GOOGLE, user.getProvider());
            assertEquals(providerAccountId, user.getProviderAccountId());
            assertTrue(user.isEmailVerified()); // OAuth verifies email
        }
        
        @Test
        @DisplayName("Should reject LOCAL provider for OAuth registration")
        void shouldRejectLocalProviderForOAuthRegistration() {
            // Given
            Email email = Email.of(VALID_EMAIL);
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () ->
                User.registerWithOAuth(email, VALID_NAME, null, AuthProvider.LOCAL, "123")
            );
        }
        
        @Test
        @DisplayName("Should require email for registration")
        void shouldRequireEmailForRegistration() {
            // Given
            HashedPassword password = HashedPassword.fromHash(VALID_PASSWORD_HASH);
            
            // When/Then
            assertThrows(NullPointerException.class, () ->
                User.registerWithCredentials(null, VALID_NAME, password)
            );
        }
        
        @Test
        @DisplayName("Should require password for local registration")
        void shouldRequirePasswordForLocalRegistration() {
            // Given
            Email email = Email.of(VALID_EMAIL);
            
            // When/Then
            assertThrows(NullPointerException.class, () ->
                User.registerWithCredentials(email, VALID_NAME, null)
            );
        }
    }
    
    @Nested
    @DisplayName("Domain Operations")
    class DomainOperations {
        
        @Test
        @DisplayName("Should record login and emit event")
        void shouldRecordLoginAndEmitEvent() {
            // Given
            User user = createLocalUser();
            user.clearDomainEvents(); // Clear registration event
            String ipAddress = "192.168.1.1";
            
            // When
            user.recordLogin(ipAddress);
            
            // Then
            assertNotNull(user.getLastLoginAt());
            
            var events = user.getDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(UserLoggedIn.class, events.get(0));
            
            UserLoggedIn event = (UserLoggedIn) events.get(0);
            assertEquals(ipAddress, event.ipAddress());
        }
        
        @Test
        @DisplayName("Should not allow login for inactive user")
        void shouldNotAllowLoginForInactiveUser() {
            // Given
            User user = createLocalUser();
            user.deactivate();
            
            // When/Then
            assertThrows(IllegalStateException.class, () -> user.recordLogin("192.168.1.1"));
        }
        
        @Test
        @DisplayName("Should update profile")
        void shouldUpdateProfile() {
            // Given
            User user = createLocalUser();
            String newName = "Jane Doe";
            String newImageUrl = "https://example.com/new-avatar.jpg";
            
            // When
            user.updateProfile(newName, newImageUrl);
            
            // Then
            assertEquals(newName, user.getName());
            assertEquals(newImageUrl, user.getImageUrl());
        }
        
        @Test
        @DisplayName("Should sync from OAuth")
        void shouldSyncFromOAuth() {
            // Given
            User user = createOAuthUser();
            String newName = "Updated Name";
            String newImageUrl = "https://example.com/updated.jpg";
            
            // When
            user.syncFromOAuth(newName, newImageUrl);
            
            // Then
            assertEquals(newName, user.getName());
            assertEquals(newImageUrl, user.getImageUrl());
            assertNotNull(user.getLastLoginAt());
        }
        
        @Test
        @DisplayName("Should change password for local user")
        void shouldChangePasswordForLocalUser() {
            // Given
            User user = createLocalUser();
            user.clearDomainEvents();
            // Valid BCrypt hash format
            HashedPassword newPassword = HashedPassword.fromHash(
                "$2a$12$S.1w6jVHJWFP5q2J8V1xVeB1b.X6Z7q3K9L0M1N2O3P4Q5R6S7T8U"
            );
            
            // When
            user.changePassword(newPassword);
            
            // Then
            assertEquals(newPassword, user.getPasswordHash());
            
            var events = user.getDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(PasswordResetCompleted.class, events.get(0));
        }
        
        @Test
        @DisplayName("Should not change password for OAuth user")
        void shouldNotChangePasswordForOAuthUser() {
            // Given
            User user = createOAuthUser();
            HashedPassword newPassword = HashedPassword.fromHash(VALID_PASSWORD_HASH);
            
            // When/Then
            assertThrows(InvalidPasswordException.class, () -> user.changePassword(newPassword));
        }
        
        @Test
        @DisplayName("Should request password reset for local user")
        void shouldRequestPasswordResetForLocalUser() {
            // Given
            User user = createLocalUser();
            user.clearDomainEvents();
            String token = "reset-token-123";
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
            
            // When
            user.requestPasswordReset(token, expiresAt);
            
            // Then
            var events = user.getDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(PasswordResetRequested.class, events.get(0));
            
            PasswordResetRequested event = (PasswordResetRequested) events.get(0);
            assertEquals(token, event.token());
            assertEquals(expiresAt, event.expiresAt());
        }
        
        @Test
        @DisplayName("Should not request password reset for OAuth user")
        void shouldNotRequestPasswordResetForOAuthUser() {
            // Given
            User user = createOAuthUser();
            
            // When/Then
            assertThrows(InvalidPasswordException.class, () ->
                user.requestPasswordReset("token", LocalDateTime.now().plusHours(1))
            );
        }
        
        @Test
        @DisplayName("Should verify email")
        void shouldVerifyEmail() {
            // Given
            User user = createLocalUser();
            assertFalse(user.isEmailVerified());
            
            // When
            user.verifyEmail();
            
            // Then
            assertTrue(user.isEmailVerified());
        }
        
        @Test
        @DisplayName("Should deactivate and reactivate user")
        void shouldDeactivateAndReactivateUser() {
            // Given
            User user = createLocalUser();
            assertTrue(user.isActive());
            
            // When deactivate
            user.deactivate();
            
            // Then
            assertFalse(user.isActive());
            
            // When reactivate
            user.reactivate();
            
            // Then
            assertTrue(user.isActive());
        }
        
        @Test
        @DisplayName("Should promote to admin")
        void shouldPromoteToAdmin() {
            // Given
            User user = createLocalUser();
            assertEquals(UserRole.USER, user.getRole());
            assertFalse(user.isAdmin());
            
            // When
            user.promoteToAdmin();
            
            // Then
            assertEquals(UserRole.ADMIN, user.getRole());
            assertTrue(user.isAdmin());
        }
    }
    
    @Nested
    @DisplayName("Query Methods")
    class QueryMethods {
        
        @Test
        @DisplayName("Local user can authenticate with password")
        void localUserCanAuthenticateWithPassword() {
            // Given
            User user = createLocalUser();
            
            // Then
            assertTrue(user.canAuthenticateWithPassword());
        }
        
        @Test
        @DisplayName("OAuth user cannot authenticate with password")
        void oauthUserCannotAuthenticateWithPassword() {
            // Given
            User user = createOAuthUser();
            
            // Then
            assertFalse(user.canAuthenticateWithPassword());
        }
    }
    
    // Test Helpers
    
    private User createLocalUser() {
        Email email = Email.of(VALID_EMAIL);
        HashedPassword password = HashedPassword.fromHash(VALID_PASSWORD_HASH);
        return User.registerWithCredentials(email, VALID_NAME, password);
    }
    
    private User createOAuthUser() {
        Email email = Email.of(VALID_EMAIL);
        return User.registerWithOAuth(
            email, VALID_NAME, null, AuthProvider.GOOGLE, "google-12345"
        );
    }
}

