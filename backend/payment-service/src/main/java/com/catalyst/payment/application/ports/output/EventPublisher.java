package com.catalyst.payment.application.ports.output;

import com.catalyst.payment.domain.event.DomainEvent;

/**
 * Output port for publishing domain events.
 */
public interface EventPublisher {
    
    /**
     * Publishes a domain event.
     *
     * @param event the domain event to publish
     * @param correlationId the correlation ID for tracing
     */
    void publish(DomainEvent event, String correlationId);

    /**
     * Publishes a domain event to a specific topic.
     *
     * @param event the domain event to publish
     * @param topic the target topic
     * @param correlationId the correlation ID for tracing
     */
    void publish(DomainEvent event, String topic, String correlationId);
}

