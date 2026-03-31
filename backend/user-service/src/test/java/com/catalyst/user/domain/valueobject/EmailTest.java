package com.catalyst.user.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Email value object.
 *
 * @author Catalyst Team
 * @since 0.1.0
 */
@DisplayName("Email Value Object")
class EmailTest {

    @Nested
    @DisplayName("Valid Emails")
    class ValidEmails {

        @ParameterizedTest
        @DisplayName("of_whenValidFormat_thenCreatesEmail")
        @ValueSource(strings = {
            "simple@example.com",
            "very.common@example.com",
            "user+tag@example.com",
            "x@example.com",
            "user@subdomain.example.com",
            "user@example.co.uk",
            "test123@test.org"
        })
        void of_whenValidFormat_thenCreatesEmail(String email) {
            Email result = Email.of(email);

            assertThat(result).isNotNull();
            assertThat(result.getValue()).isEqualTo(email.toLowerCase());
        }

        @Test
        @DisplayName("of_whenUppercaseEmail_thenNormalizesToLowercase")
        void of_whenUppercaseEmail_thenNormalizesToLowercase() {
            Email result = Email.of("TEST@EXAMPLE.COM");

            assertThat(result.getValue()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("of_whenEmailWithWhitespace_thenTrimsAndCreates")
        void of_whenEmailWithWhitespace_thenTrimsAndCreates() {
            Email result = Email.of("  test@example.com  ");

            assertThat(result.getValue()).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("Invalid Emails")
    class InvalidEmails {

        @ParameterizedTest
        @DisplayName("of_whenInvalidFormat_thenThrowsIllegalArgument")
        @ValueSource(strings = {
            "plainaddress",
            "@missing-local.com",
            "missing-at.com",
            "missing@.com",
            "missing@domain",
            "two@@at.com",
            "spaces in@email.com"
        })
        void of_whenInvalidFormat_thenThrowsIllegalArgument(String email) {
            assertThatThrownBy(() -> Email.of(email))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("of_whenNull_thenThrowsIllegalArgument")
        void of_whenNull_thenThrowsIllegalArgument() {
            assertThatThrownBy(() -> Email.of(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("of_whenBlank_thenThrowsIllegalArgument")
        void of_whenBlank_thenThrowsIllegalArgument() {
            assertThatThrownBy(() -> Email.of("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Email Parts")
    class EmailParts {

        @Test
        @DisplayName("getDomain_whenValidEmail_thenReturnsDomainPart")
        void getDomain_whenValidEmail_thenReturnsDomainPart() {
            Email email = Email.of("user@example.com");

            assertThat(email.getDomain()).isEqualTo("example.com");
        }

        @Test
        @DisplayName("getLocalPart_whenValidEmail_thenReturnsLocalPart")
        void getLocalPart_whenValidEmail_thenReturnsLocalPart() {
            Email email = Email.of("user.name@example.com");

            assertThat(email.getLocalPart()).isEqualTo("user.name");
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("equals_whenSameEmail_thenReturnsTrue")
        void equals_whenSameEmail_thenReturnsTrue() {
            Email email1 = Email.of("test@example.com");
            Email email2 = Email.of("test@example.com");

            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("equals_whenDifferentEmails_thenReturnsFalse")
        void equals_whenDifferentEmails_thenReturnsFalse() {
            Email email1 = Email.of("test1@example.com");
            Email email2 = Email.of("test2@example.com");

            assertThat(email1).isNotEqualTo(email2);
        }

        @Test
        @DisplayName("equals_whenSameEmailDifferentCase_thenReturnsTrueAfterNormalization")
        void equals_whenSameEmailDifferentCase_thenReturnsTrueAfterNormalization() {
            Email email1 = Email.of("TEST@EXAMPLE.COM");
            Email email2 = Email.of("test@example.com");

            assertThat(email1).isEqualTo(email2);
        }
    }
}
