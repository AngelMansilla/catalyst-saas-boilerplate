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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        @DisplayName("registerWithCredentials_whenValidInput_thenCreatesLocalUser")
        void registerWithCredentials_whenValidInput_thenCreatesLocalUser() {
            Email email = Email.of(VALID_EMAIL);
            HashedPassword password = HashedPassword.fromHash(VALID_PASSWORD_HASH);

            User user = User.registerWithCredentials(email, VALID_NAME, password);

            assertThat(user.getId()).isNotNull();
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getName()).isEqualTo(VALID_NAME);
            assertThat(user.getPasswordHash()).isEqualTo(password);
            assertThat(user.getProvider()).isEqualTo(AuthProvider.LOCAL);
            assertThat(user.getProviderAccountId()).isNull();
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
            assertThat(user.isEmailVerified()).isFalse();
            assertThat(user.isActive()).isTrue();
            assertThat(user.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("registerWithCredentials_whenValidInput_thenEmitsUserRegisteredEvent")
        void registerWithCredentials_whenValidInput_thenEmitsUserRegisteredEvent() {
            Email email = Email.of(VALID_EMAIL);
            HashedPassword password = HashedPassword.fromHash(VALID_PASSWORD_HASH);

            User user = User.registerWithCredentials(email, VALID_NAME, password);

            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserRegistered.class);

            UserRegistered event = (UserRegistered) user.getDomainEvents().get(0);
            assertThat(event.userId()).isEqualTo(user.getId());
            assertThat(event.email()).isEqualTo(email);
            assertThat(event.provider()).isEqualTo(AuthProvider.LOCAL);
        }

        @Test
        @DisplayName("registerWithOAuth_whenValidInput_thenCreatesOAuthUser")
        void registerWithOAuth_whenValidInput_thenCreatesOAuthUser() {
            Email email = Email.of(VALID_EMAIL);
            String imageUrl = "https://example.com/avatar.jpg";
            String providerAccountId = "google-12345";

            User user = User.registerWithOAuth(email, VALID_NAME, imageUrl, AuthProvider.GOOGLE, providerAccountId);

            assertThat(user.getId()).isNotNull();
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getName()).isEqualTo(VALID_NAME);
            assertThat(user.getImageUrl()).isEqualTo(imageUrl);
            assertThat(user.getPasswordHash()).isNull();
            assertThat(user.getProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(user.getProviderAccountId()).isEqualTo(providerAccountId);
            assertThat(user.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("registerWithOAuth_whenLocalProvider_thenThrowsIllegalArgument")
        void registerWithOAuth_whenLocalProvider_thenThrowsIllegalArgument() {
            Email email = Email.of(VALID_EMAIL);

            assertThatThrownBy(() -> User.registerWithOAuth(email, VALID_NAME, null, AuthProvider.LOCAL, "123"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("registerWithCredentials_whenNullEmail_thenThrowsNullPointer")
        void registerWithCredentials_whenNullEmail_thenThrowsNullPointer() {
            HashedPassword password = HashedPassword.fromHash(VALID_PASSWORD_HASH);

            assertThatThrownBy(() -> User.registerWithCredentials(null, VALID_NAME, password))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("registerWithCredentials_whenNullPassword_thenThrowsNullPointer")
        void registerWithCredentials_whenNullPassword_thenThrowsNullPointer() {
            Email email = Email.of(VALID_EMAIL);

            assertThatThrownBy(() -> User.registerWithCredentials(email, VALID_NAME, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Domain Operations")
    class DomainOperations {

        @Test
        @DisplayName("recordLogin_whenActiveUser_thenUpdatesLastLoginAndEmitsEvent")
        void recordLogin_whenActiveUser_thenUpdatesLastLoginAndEmitsEvent() {
            User user = createLocalUser();
            user.clearDomainEvents();
            String ipAddress = "192.168.1.1";

            user.recordLogin(ipAddress);

            assertThat(user.getLastLoginAt()).isNotNull();
            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserLoggedIn.class);

            UserLoggedIn event = (UserLoggedIn) user.getDomainEvents().get(0);
            assertThat(event.ipAddress()).isEqualTo(ipAddress);
        }

        @Test
        @DisplayName("recordLogin_whenInactiveUser_thenThrowsIllegalState")
        void recordLogin_whenInactiveUser_thenThrowsIllegalState() {
            User user = createLocalUser();
            user.deactivate();

            assertThatThrownBy(() -> user.recordLogin("192.168.1.1"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("updateProfile_whenValidInput_thenUpdatesNameAndImage")
        void updateProfile_whenValidInput_thenUpdatesNameAndImage() {
            User user = createLocalUser();
            String newName = "Jane Doe";
            String newImageUrl = "https://example.com/new-avatar.jpg";

            user.updateProfile(newName, newImageUrl);

            assertThat(user.getName()).isEqualTo(newName);
            assertThat(user.getImageUrl()).isEqualTo(newImageUrl);
        }

        @Test
        @DisplayName("syncFromOAuth_whenOAuthUser_thenUpdatesProfileAndLastLogin")
        void syncFromOAuth_whenOAuthUser_thenUpdatesProfileAndLastLogin() {
            User user = createOAuthUser();
            String newName = "Updated Name";
            String newImageUrl = "https://example.com/updated.jpg";

            user.syncFromOAuth(newName, newImageUrl);

            assertThat(user.getName()).isEqualTo(newName);
            assertThat(user.getImageUrl()).isEqualTo(newImageUrl);
            assertThat(user.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("changePassword_whenLocalUser_thenUpdatesPasswordAndEmitsEvent")
        void changePassword_whenLocalUser_thenUpdatesPasswordAndEmitsEvent() {
            User user = createLocalUser();
            user.clearDomainEvents();
            HashedPassword newPassword = HashedPassword.fromHash(
                    "$2a$12$S.1w6jVHJWFP5q2J8V1xVeB1b.X6Z7q3K9L0M1N2O3P4Q5R6S7T8U");

            user.changePassword(newPassword);

            assertThat(user.getPasswordHash()).isEqualTo(newPassword);
            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(PasswordResetCompleted.class);
        }

        @Test
        @DisplayName("changePassword_whenOAuthUser_thenThrowsInvalidPassword")
        void changePassword_whenOAuthUser_thenThrowsInvalidPassword() {
            User user = createOAuthUser();
            HashedPassword newPassword = HashedPassword.fromHash(VALID_PASSWORD_HASH);

            assertThatThrownBy(() -> user.changePassword(newPassword))
                    .isInstanceOf(InvalidPasswordException.class);
        }

        @Test
        @DisplayName("requestPasswordReset_whenLocalUser_thenEmitsPasswordResetRequestedEvent")
        void requestPasswordReset_whenLocalUser_thenEmitsPasswordResetRequestedEvent() {
            User user = createLocalUser();
            user.clearDomainEvents();
            String token = "reset-token-123";
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

            user.requestPasswordReset(token, expiresAt);

            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(PasswordResetRequested.class);

            PasswordResetRequested event = (PasswordResetRequested) user.getDomainEvents().get(0);
            assertThat(event.token()).isEqualTo(token);
            assertThat(event.expiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("requestPasswordReset_whenOAuthUser_thenThrowsInvalidPassword")
        void requestPasswordReset_whenOAuthUser_thenThrowsInvalidPassword() {
            User user = createOAuthUser();

            assertThatThrownBy(() -> user.requestPasswordReset("token", LocalDateTime.now().plusHours(1)))
                    .isInstanceOf(InvalidPasswordException.class);
        }

        @Test
        @DisplayName("verifyEmail_whenNotVerified_thenSetsEmailVerifiedTrue")
        void verifyEmail_whenNotVerified_thenSetsEmailVerifiedTrue() {
            User user = createLocalUser();
            assertThat(user.isEmailVerified()).isFalse();

            user.verifyEmail();

            assertThat(user.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("deactivate_whenActiveUser_thenIsActiveBecomesFalse")
        void deactivate_whenActiveUser_thenIsActiveBecomesFalse() {
            User user = createLocalUser();
            assertThat(user.isActive()).isTrue();

            user.deactivate();

            assertThat(user.isActive()).isFalse();
        }

        @Test
        @DisplayName("reactivate_whenInactiveUser_thenIsActiveBecomesTrue")
        void reactivate_whenInactiveUser_thenIsActiveBecomesTrue() {
            User user = createLocalUser();
            user.deactivate();
            assertThat(user.isActive()).isFalse();

            user.reactivate();

            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("promoteToAdmin_whenRegularUser_thenRoleBecomesAdmin")
        void promoteToAdmin_whenRegularUser_thenRoleBecomesAdmin() {
            User user = createLocalUser();
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
            assertThat(user.isAdmin()).isFalse();

            user.promoteToAdmin();

            assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
            assertThat(user.isAdmin()).isTrue();
        }
    }

    @Nested
    @DisplayName("Query Methods")
    class QueryMethods {

        @Test
        @DisplayName("canAuthenticateWithPassword_whenLocalUser_thenReturnsTrue")
        void canAuthenticateWithPassword_whenLocalUser_thenReturnsTrue() {
            User user = createLocalUser();

            assertThat(user.canAuthenticateWithPassword()).isTrue();
        }

        @Test
        @DisplayName("canAuthenticateWithPassword_whenOAuthUser_thenReturnsFalse")
        void canAuthenticateWithPassword_whenOAuthUser_thenReturnsFalse() {
            User user = createOAuthUser();

            assertThat(user.canAuthenticateWithPassword()).isFalse();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User createLocalUser() {
        return User.registerWithCredentials(
                Email.of(VALID_EMAIL),
                VALID_NAME,
                HashedPassword.fromHash(VALID_PASSWORD_HASH));
    }

    private User createOAuthUser() {
        return User.registerWithOAuth(
                Email.of(VALID_EMAIL),
                VALID_NAME,
                null,
                AuthProvider.GOOGLE,
                "google-12345");
    }
}
