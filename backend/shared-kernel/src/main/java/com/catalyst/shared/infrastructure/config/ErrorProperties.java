package com.catalyst.shared.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for error handling.
 */
@Validated
@ConfigurationProperties(prefix = "catalyst.error")
public class ErrorProperties {
    
    /**
     * Whether to include stack traces in error responses.
     * Should be false in production.
     */
    private boolean includeStacktrace = false;
    
    public boolean isIncludeStacktrace() {
        return includeStacktrace;
    }
    
    public void setIncludeStacktrace(boolean includeStacktrace) {
        this.includeStacktrace = includeStacktrace;
    }
}