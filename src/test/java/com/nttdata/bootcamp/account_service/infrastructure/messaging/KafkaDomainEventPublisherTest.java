package com.nttdata.bootcamp.account_service.infrastructure.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.nttdata.bootcamp.account_service.domain.event.DebitCardPaymentEvent;
import io.reactivex.rxjava3.observers.TestObserver;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

/**
 * Unit test suite for {@link KafkaDomainEventPublisher}.
 */
@ExtendWith(MockitoExtension.class)
class KafkaDomainEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaDomainEventPublisher publisher;

    @Test
    @DisplayName("Should complete when the event is published")
    void publish_WhenSuccess_ShouldComplete() {
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

        DebitCardPaymentEvent event = new DebitCardPaymentEvent("ACC-001", "CARD-001", 50.0, 0L);

        TestObserver<Void> observer = publisher.publish("debit-card-payments", event).test();

        observer.assertComplete();
    }

    @Test
    @DisplayName("Should complete even when publishing fails (best-effort)")
    void publish_WhenFailure_ShouldCompleteGracefully() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("broker down"));
        when(kafkaTemplate.send(anyString(), any())).thenReturn(future);

        DebitCardPaymentEvent event = new DebitCardPaymentEvent("ACC-001", "CARD-001", 50.0, 0L);

        TestObserver<Void> observer = publisher.publish("debit-card-payments", event).test();

        observer.assertComplete();
    }
}
