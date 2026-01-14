package com.catalyst.notification.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        @DisplayName("Should create valid email address")
        void shouldCreateValidEmailAddress() {
            // Given
            String validEmail = "test@example.com";
            
            // When
            EmailAddress emailAddress = new EmailAddress(validEmail);
            
            // Then
            assertNotNull(emailAddress);
            assertEquals(validEmail, emailAddress.value());
        }
        
        @Test
        @DisplayName("Should accept email with subdomain")
        void shouldAcceptEmailWithSubdomain() {
            // Given
            String email = "user@mail.example.com";
            
            // When
            EmailAddress emailAddress = new EmailAddress(email);
            
            // Then
            assertEquals(email, emailAddress.value());
        }
        
        @Test
        @DisplayName("Should accept email with plus sign")
        void shouldAcceptEmailWithPlusSign() {
            // Given
            String email = "user+tag@example.com";
            
            // When
            EmailAddress emailAddress = new EmailAddress(email);
            
            // Then
            assertEquals(email, emailAddress.value());
        }
        
        @Test
        @DisplayName("Should accept email with dots")
        void shouldAcceptEmailWithDots() {
            // Given
            String email = "first.last@example.com";
            
            // When
            EmailAddress emailAddress = new EmailAddress(email);
            
            // Then
            assertEquals(email, emailAddress.value());
        }
        
        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                new EmailAddress(null);
            });
        }
        
        @Test
        @DisplayName("Should throw exception when email is blank")
        void shouldThrowExceptionWhenEmailIsBlank() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                new EmailAddress("   ");
            });
        }
        
        @Test
        @DisplayName("Should throw exception when email has no @ symbol")
        void shouldThrowExceptionWhenEmailHasNoAtSymbol() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                new EmailAddress("invalidemail.com");
            });
        }
        
        @Test
        @DisplayName("Should throw exception when email has no domain")
        void shouldThrowExceptionWhenEmailHasNoDomain() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                new EmailAddress("user@");
            });
        }
        
        @Test
        @DisplayName("Should throw exception when email has no local part")
        void shouldThrowExceptionWhenEmailHasNoLocalPart() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                new EmailAddress("@example.com");
            });
        }
        
        @Test
        @DisplayName("Should throw exception when email has invalid domain")
        void shouldThrowExceptionWhenEmailHasInvalidDomain() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                new EmailAddress("user@invalid");
            });
        }
    }
    
    @Nested
    @DisplayName("toString")
    class ToString {
        
        @Test
        @DisplayName("Should return email value as string")
        void shouldReturnEmailValueAsString() {
            // Given
            String email = "test@example.com";
            EmailAddress emailAddress = new EmailAddress(email);
            
            // When
            String result = emailAddress.toString();
            
            // Then
            assertEquals(email, result);
        }
    }
}

