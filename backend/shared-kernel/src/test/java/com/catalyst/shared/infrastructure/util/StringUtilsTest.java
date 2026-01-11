package com.catalyst.shared.infrastructure.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for StringUtils.
 */
class StringUtilsTest {
    
    @Test
    @DisplayName("isBlank should handle null and empty strings")
    void isBlankShouldHandleNullAndEmpty() {
        assertThat(StringUtils.isBlank(null)).isTrue();
        assertThat(StringUtils.isBlank("")).isTrue();
        assertThat(StringUtils.isBlank("   ")).isTrue();
        assertThat(StringUtils.isBlank("text")).isFalse();
    }
    
    @Test
    @DisplayName("truncate should limit string length")
    void truncateShouldLimitLength() {
        assertThat(StringUtils.truncate("Hello World", 5)).isEqualTo("Hello");
        assertThat(StringUtils.truncate("Hi", 10)).isEqualTo("Hi");
        assertThat(StringUtils.truncate(null, 5)).isNull();
    }
    
    @Test
    @DisplayName("truncateWithEllipsis should add ellipsis")
    void truncateWithEllipsisShouldAddEllipsis() {
        assertThat(StringUtils.truncateWithEllipsis("Hello World", 8))
            .isEqualTo("Hello...");
        assertThat(StringUtils.truncateWithEllipsis("Hi", 10)).isEqualTo("Hi");
    }
    
    @Test
    @DisplayName("toSlug should create URL-safe slug")
    void toSlugShouldCreateUrlSafeSlug() {
        assertThat(StringUtils.toSlug("Hello World")).isEqualTo("hello-world");
        assertThat(StringUtils.toSlug("  Multiple   Spaces  ")).isEqualTo("multiple-spaces");
        assertThat(StringUtils.toSlug("Café & Résumé")).isEqualTo("cafe-resume");
        assertThat(StringUtils.toSlug(null)).isNull();
    }
    
    @Test
    @DisplayName("stripHtml should remove HTML tags")
    void stripHtmlShouldRemoveTags() {
        assertThat(StringUtils.stripHtml("<p>Hello</p>")).isEqualTo("Hello");
        assertThat(StringUtils.stripHtml("<script>alert('xss')</script>")).isEmpty();
        assertThat(StringUtils.stripHtml("No tags")).isEqualTo("No tags");
    }
    
    @Test
    @DisplayName("escapeHtml should escape special characters")
    void escapeHtmlShouldEscapeSpecialChars() {
        assertThat(StringUtils.escapeHtml("<script>")).isEqualTo("&lt;script&gt;");
        assertThat(StringUtils.escapeHtml("\"quote\"")).isEqualTo("&quot;quote&quot;");
        assertThat(StringUtils.escapeHtml("&")).isEqualTo("&amp;");
    }
    
    @Test
    @DisplayName("maskEmail should mask email address")
    void maskEmailShouldMaskAddress() {
        assertThat(StringUtils.maskEmail("john.doe@example.com"))
            .isEqualTo("j***e@example.com");
        assertThat(StringUtils.maskEmail("ab@test.com"))
            .isEqualTo("a***@test.com"); // 2 chars or less uses only first char
        assertThat(StringUtils.maskEmail("a@test.com"))
            .isEqualTo("a***@test.com");
    }
    
    @Test
    @DisplayName("maskCardNumber should show only last 4 digits")
    void maskCardNumberShouldShowLastFour() {
        assertThat(StringUtils.maskCardNumber("4111111111111111")).isEqualTo("****1111");
        assertThat(StringUtils.maskCardNumber("1234")).isEqualTo("****1234");
    }
    
    @Test
    @DisplayName("toSnakeCase should convert camelCase")
    void toSnakeCaseShouldConvertCamelCase() {
        assertThat(StringUtils.toSnakeCase("camelCase")).isEqualTo("camel_case");
        assertThat(StringUtils.toSnakeCase("HttpRequest")).isEqualTo("http_request");
        assertThat(StringUtils.toSnakeCase("simple")).isEqualTo("simple");
    }
    
    @Test
    @DisplayName("toCamelCase should convert snake_case")
    void toCamelCaseShouldConvertSnakeCase() {
        assertThat(StringUtils.toCamelCase("snake_case")).isEqualTo("snakeCase");
        assertThat(StringUtils.toCamelCase("simple")).isEqualTo("simple");
        assertThat(StringUtils.toCamelCase("UPPER_CASE")).isEqualTo("upperCase");
    }
}

