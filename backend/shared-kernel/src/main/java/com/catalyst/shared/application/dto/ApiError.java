package com.catalyst.shared.application.dto;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard API error response format.
 * Used for consistent error responses across all endpoints.
 */
public record ApiError(
    String code,
    String message,
    int status,
    String path,
    Instant timestamp,
    Map<String, Object> details
) {
    
    /**
     * Creates a builder for ApiError.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Converts this error to a standard response format.
     */
    public Map<String, Object> toResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        error.put("status", status);
        error.put("path", path);
        error.put("timestamp", timestamp.toString());
        
        if (details != null && !details.isEmpty()) {
            error.put("details", details);
        }
        
        response.put("error", error);
        return response;
    }
    
    /**
     * Builder for ApiError.
     */
    public static class Builder {
        private String code;
        private String message;
        private int status;
        private String path;
        private Instant timestamp = Instant.now();
        private Map<String, Object> details = new HashMap<>();
        
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder status(int status) {
            this.status = status;
            return this;
        }
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder details(Map<String, Object> details) {
            this.details = details != null ? new HashMap<>(details) : new HashMap<>();
            return this;
        }
        
        public Builder addDetail(String key, Object value) {
            this.details.put(key, value);
            return this;
        }
        
        public ApiError build() {
            return new ApiError(code, message, status, path, timestamp, details);
        }
    }
}

