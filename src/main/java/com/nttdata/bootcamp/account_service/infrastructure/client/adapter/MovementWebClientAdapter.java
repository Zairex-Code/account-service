package com.nttdata.bootcamp.account_service.infrastructure.client.adapter;

import com.nttdata.bootcamp.account_service.domain.port.output.MovementClientPort;
import io.reactivex.rxjava3.core.Completable;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

/**
 * Reactive WebClient adapter implementation for {@link MovementClientPort} interfacing
 * with transaction-service.
 * <p>
 * Technical & Business Rules:
 * - Records monetary movements asynchronously via HTTP POST to transaction-service.
 * - Resolves service URLs dynamically via Eureka Service Discovery (http://transaction-service).
 * - Movement recording is best-effort: failures are logged and swallowed to avoid blocking
 *   the core business operation.
 * - Bridges Reactor (WebClient) responses to RxJava 3 via {@link RxJava3Adapter}.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MovementWebClientAdapter implements MovementClientPort {

    private final WebClient.Builder webClientBuilder;

    private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

    /**
     * Records a monetary movement in the transaction-service ledger.
     *
     * @param productId    Unique product database identifier (account ID).
     * @param productType  Bank product type (e.g., ACCOUNT).
     * @param movementType Type of movement (e.g., DEPOSIT, WITHDRAWAL).
     * @param amount       Monetary amount of the movement.
     * @return A Completable that completes when the movement has been recorded.
     */
    @Override
    public Completable recordMovement(String productId, String productType,
                                      String movementType, Double amount) {
        log.debug("Initiating movement recording for product ID: {}, type: {}",
                productId, movementType);

        Map<String, Object> body = Map.of(
                "productId", productId,
                "productType", productType,
                "movementType", movementType,
                "amount", amount);

        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("transaction-service");

        Mono<Void> call = webClientBuilder.build()
                .post()
                .uri("http://transaction-service/api/v1/movements")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .then()
                .timeout(Duration.ofSeconds(2));

        Mono<Void> guarded = circuitBreaker.run(call, throwable -> {
            log.warn("Movement recording failed for product ID: {}. Error: {}",
                    productId, throwable.getMessage());
            return Mono.empty();
        });

        return RxJava3Adapter.monoToCompletable(guarded)
                .doOnComplete(() -> log.debug("Movement recorded for product ID: {}", productId));
    }
}
