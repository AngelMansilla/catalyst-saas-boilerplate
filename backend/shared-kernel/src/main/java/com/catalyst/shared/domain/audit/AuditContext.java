package com.catalyst.shared.domain.audit;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Context holder for audit information.
 * Provides access to current user and timestamp for audit operations.
 */
public record AuditContext(
    UUID userId,
    String userEmail,
    Instant timestamp
) {
    
    private static final ThreadLocal<AuditContext> CONTEXT = new ThreadLocal<>();
    
    /**
     * Creates an audit context for the current timestamp.
     */
    public static AuditContext of(UUID userId, String userEmail) {
        return new AuditContext(userId, userEmail, Instant.now());
    }
    
    /**
     * Creates a system audit context for automated operations.
     */
    public static AuditContext system() {
        return new AuditContext(
            UUID.fromString("00000000-0000-0000-0000-000000000000"),
            "system@catalyst.local",
            Instant.now()
        );
    }
    
    /**
     * Sets the current audit context for this thread.
     */
    public static void set(AuditContext context) {
        CONTEXT.set(context);
    }
    
    /**
     * Gets the current audit context.
     */
    public static Optional<AuditContext> current() {
        return Optional.ofNullable(CONTEXT.get());
    }
    
    /**
     * Gets the current audit context or throws if not set.
     */
    public static AuditContext require() {
        return current().orElseThrow(() -> 
            new IllegalStateException("Audit context not set"));
    }
    
    /**
     * Gets the current user ID.
     */
    public static Optional<UUID> currentUserId() {
        return current().map(AuditContext::userId);
    }
    
    /**
     * Clears the current audit context.
     */
    public static void clear() {
        CONTEXT.remove();
    }
    
    /**
     * Executes a runnable with the specified audit context.
     */
    public static void with(AuditContext context, Runnable runnable) {
        try {
            set(context);
            runnable.run();
        } finally {
            clear();
        }
    }
}

