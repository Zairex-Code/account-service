package com.nttdata.bootcamp.account_service.infrastructure.messaging;

import com.nttdata.bootcamp.account_service.domain.port.output.DomainEventPublisher;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka implementation of the {@link DomainEventPublisher} port.
 * <p>
 * Technical & Business Rules:
 * - Publishes events asynchronously via {@link KafkaTemplate}.
 * - Best-effort: failures are logged and swallowed.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Completable publish(String topic, Object event) {
        log.debug("Publishing domain event to topic '{}': {}", topic, event.getClass().getSimpleName());
        return Completable.create(emitter -> kafkaTemplate.send(topic, event)
                        .whenComplete((result, throwable) -> {
                            if (throwable != null) {
                                emitter.onError(throwable);
                            } else {
                                emitter.onComplete();
                            }
                        }))
                .doOnComplete(() -> log.debug("Domain event published to topic '{}'", topic))
                .onErrorComplete(throwable -> {
                    log.warn("Failed to publish domain event to topic '{}'. Error: {}",
                            topic, throwable.getMessage());
                    return true;
                });
    }
}
