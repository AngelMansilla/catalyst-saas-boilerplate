package com.catalyst.shared.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Configuration properties for CORS settings.
 * 
 * <p>Configure in application.yml:
 * <pre>
 * catalyst:
 *   security:
 *     cors:
 *       allowed-origins:
 *         - http://localhost:3000
 *         - https://catalyst.example.com
 *       allowed-methods:
 *         - GET
 *         - POST
 *         - PUT
 *         - DELETE
 *       max-age: 3600
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "catalyst.security.cors")
public class CorsProperties {
    
    /**
     * List of allowed origins for CORS.
     */
    private List<String> allowedOrigins = List.of("http://localhost:3000");
    
    /**
     * List of allowed HTTP methods.
     */
    private List<String> allowedMethods = List.of(
        "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    );
    
    /**
     * List of allowed headers.
     */
    private List<String> allowedHeaders = List.of(
        "Authorization", "Content-Type", "X-Requested-With", "Accept", 
        "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
    );
    
    /**
     * List of headers to expose to the client.
     */
    private List<String> exposedHeaders = List.of(
        "X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset"
    );
    
    /**
     * Whether to allow credentials (cookies, authorization headers).
     */
    private boolean allowCredentials = true;
    
    /**
     * Max age of the CORS preflight cache in seconds.
     */
    private long maxAge = 3600L;
    
    // Getters and Setters
    
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
    
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
    
    public List<String> getAllowedMethods() {
        return allowedMethods;
    }
    
    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }
    
    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }
    
    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }
    
    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }
    
    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }
    
    public boolean isAllowCredentials() {
        return allowCredentials;
    }
    
    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }
    
    public long getMaxAge() {
        return maxAge;
    }
    
    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }
}

