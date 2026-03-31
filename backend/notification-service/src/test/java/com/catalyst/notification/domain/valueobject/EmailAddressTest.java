package com.catalyst.notification.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for EmailAddress value object.
 *
 * @author Catalyst Team
 * @since 0.1.0
 */
@DisplayName("EmailAddress Value Object")
class EmailAddressTest {

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("constructor_whenValidEmail_thenCreatesEmailAddress")
        void constructor_whenValidEmail_thenCreatesEmailAddress() {
            String validEmail = "test@example.com";

            EmailAddress emailAddress = new EmailAddress(validEmail);

            assertThat(emailAddress).isNotNull();
            assertThat(emailAddress.value()).isEqualTo(validEmail);
        }

        @Test
        @DisplayName("constructor_whenEmailWithSubdomain_thenCreates")
        void constructor_whenEmailWithSubdomain_thenCreates() {
            String email = "user@mail.example.com";

            EmailAddress emailAddress = new EmailAddress(email);

            assertThat(emailAddress.value()).isEqualTo(email);
        }

        @Test
        @DisplayName("constructor_whenEmailWithPlusSign_thenCreates")
        void constructor_whenEmailWithPlusSign_thenCreates() {
            String email = "user+tag@example.com";

            EmailAddress emailAddress = new EmailAddress(email);

            assertThat(emailAddress.value()).isEqualTo(email);
        }

        @Test
        @DisplayName("constructor_whenEmailWithDots_thenCreates")
        void constructor_whenEmailWithDots_thenCreates() {
            String email = "first.last@example.com";

            EmailAddress emailAddress = new EmailAddress(email);

            assertThat(emailAddress.value()).isEqualTo(email);
        }

        @Test
        @DisplayName("constructor_whenNull_thenThrowsNullPointer")
        void constructor_whenNull_thenThrowsNullPointer() {
            assertThatThrownBy(() -> new EmailAddress(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("constructor_whenBlank_thenThrowsIllegalArgument")
        void constructor_whenBlank_thenThrowsIllegalArgument() {
            assertThatThrownBy(() -> new EmailAddress("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("constructor_whenNoAtSymbol_thenThrowsIllegalArgument")
        void constructor_whenNoAtSymbol_thenThrowsIllegalArgument() {
            assertThatThrownBy(() -> new EmailAddress("invalidemail.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("constructor_whenNoDomain_thenThrowsIllegalArgument")
        void constructor_whenNoDomain_thenThrowsIllegalArgument() {
            assertThatThrownBy(() -> new EmailAddress("user@"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("constructor_whenNoLocalPart_thenThrowsIllegalArgument")
        void constructor_whenNoLocalPart_thenThrowsIllegalArgument() {
            assertThatThrownBy(() -> new EmailAddress("@example.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("constructor_whenInvalidDomain_thenThrowsIllegalArgument")
        void constructor_whenInvalidDomain_thenThrowsIllegalArgument() {
            assertThatThrownBy(() -> new EmailAddress("user@invalid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString_whenCalled_thenReturnsEmailValue")
        void toString_whenCalled_thenReturnsEmailValue() {
            String email = "test@example.com";
            EmailAddress emailAddress = new EmailAddress(email);

            assertThat(emailAddress.toString()).isEqualTo(email);
        }
    }
}
