package com.catalyst.shared.domain.audit;

import java.time.Instant;
import java.util.UUID;

/**
 * Interface for auditable entities.
 * Provides tracking of creation and modification metadata.
 */
public interface Auditable {
    
    /**
     * Gets the creation timestamp.
     */
    Instant getCreatedAt();
    
    /**
     * Gets the ID of the user who created this entity.
     */
    UUID getCreatedBy();
    
    /**
     * Gets the last modification timestamp.
     */
    Instant getUpdatedAt();
    
    /**
     * Gets the ID of the user who last modified this entity.
     */
    UUID getUpdatedBy();
    
    /**
     * Sets the creation audit fields.
     */
    void setCreatedAudit(Instant createdAt, UUID createdBy);
    
    /**
     * Sets the update audit fields.
     */
    void setUpdatedAudit(Instant updatedAt, UUID updatedBy);
}

