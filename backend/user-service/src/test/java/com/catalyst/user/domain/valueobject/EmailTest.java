package com.catalyst.user.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

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
        @DisplayName("Should accept valid email formats")
        @ValueSource(strings = {
            "simple@example.com",
            "very.common@example.com",
            "user+tag@example.com",
            "x@example.com",
            "user@subdomain.example.com",
            "user@example.co.uk",
            "test123@test.org"
        })
        void shouldAcceptValidEmailFormats(String email) {
            // When
            Email result = Email.of(email);
            
            // Then
            assertNotNull(result);
            assertEquals(email.toLowerCase(), result.getValue());
        }
        
        @Test
        @DisplayName("Should normalize email to lowercase")
        void shouldNormalizeEmailToLowercase() {
            // When
            Email result = Email.of("TEST@EXAMPLE.COM");
            
            // Then
            assertEquals("test@example.com", result.getValue());
        }
        
        @Test
        @DisplayName("Should trim whitespace")
        void shouldTrimWhitespace() {
            // When
            Email result = Email.of("  test@example.com  ");
            
            // Then
            assertEquals("test@example.com", result.getValue());
        }
    }
    
    @Nested
    @DisplayName("Invalid Emails")
    class InvalidEmails {
        
        @ParameterizedTest
        @DisplayName("Should reject invalid email formats")
        @ValueSource(strings = {
            "plainaddress",
            "@missing-local.com",
            "missing-at.com",
            "missing@.com",
            "missing@domain",
            "two@@at.com",
            "spaces in@email.com"
        })
        void shouldRejectInvalidEmailFormats(String email) {
            // When/Then
            assertThrows(IllegalArgumentException.class, () -> Email.of(email));
        }
        
        @Test
        @DisplayName("Should reject null email")
        void shouldRejectNullEmail() {
            assertThrows(IllegalArgumentException.class, () -> Email.of(null));
        }
        
        @Test
        @DisplayName("Should reject blank email")
        void shouldRejectBlankEmail() {
            assertThrows(IllegalArgumentException.class, () -> Email.of("   "));
        }
    }
    
    @Nested
    @DisplayName("Email Parts")
    class EmailParts {
        
        @Test
        @DisplayName("Should extract domain")
        void shouldExtractDomain() {
            // Given
            Email email = Email.of("user@example.com");
            
            // Then
            assertEquals("example.com", email.getDomain());
        }
        
        @Test
        @DisplayName("Should extract local part")
        void shouldExtractLocalPart() {
            // Given
            Email email = Email.of("user.name@example.com");
            
            // Then
            assertEquals("user.name", email.getLocalPart());
        }
    }
    
    @Nested
    @DisplayName("Equality")
    class Equality {
        
        @Test
        @DisplayName("Same emails should be equal")
        void sameEmailsShouldBeEqual() {
            // Given
            Email email1 = Email.of("test@example.com");
            Email email2 = Email.of("test@example.com");
            
            // Then
            assertEquals(email1, email2);
            assertEquals(email1.hashCode(), email2.hashCode());
        }
        
        @Test
        @DisplayName("Different emails should not be equal")
        void differentEmailsShouldNotBeEqual() {
            // Given
            Email email1 = Email.of("test1@example.com");
            Email email2 = Email.of("test2@example.com");
            
            // Then
            assertNotEquals(email1, email2);
        }
        
        @Test
        @DisplayName("Case-different emails should be equal after normalization")
        void caseDifferentEmailsShouldBeEqual() {
            // Given
            Email email1 = Email.of("TEST@EXAMPLE.COM");
            Email email2 = Email.of("test@example.com");
            
            // Then
            assertEquals(email1, email2);
        }
    }
}

