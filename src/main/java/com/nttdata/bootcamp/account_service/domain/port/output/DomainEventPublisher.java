package com.nttdata.bootcamp.account_service.domain.port.output;

import io.reactivex.rxjava3.core.Completable;

/**
 * Secondary Output Port interface for publishing domain events.
 * <p>
 * Technical & Business Rules:
 * - Decouples the domain from any specific event broker (e.g., Kafka).
 * - Publishing is best-effort: failures must not break the core business flow.
 * </p>
 */
public interface DomainEventPublisher {

    /**
     * Publishes a domain event to a topic.
     *
     * @param topic Event destination topic name.
     * @param event Event payload.
     * @return A Completable that completes when the event has been published.
     */
    Completable publish(String topic, Object event);
}
