package com.catalyst.shared.domain.common;

/**
 * Marker interface for value objects in the domain.
 * Value objects are immutable and compared by value.
 * 
 * <p>All implementations must:
 * <ul>
 *   <li>Be immutable</li>
 *   <li>Override equals() and hashCode()</li>
 *   <li>Implement validation in constructor</li>
 * </ul>
 */
public interface ValueObject {
    
    /**
     * Validates the value object.
     * Should throw an exception if invalid.
     */
    default void validate() {
        // Default implementation does nothing
        // Subclasses can override to add validation
    }
}

