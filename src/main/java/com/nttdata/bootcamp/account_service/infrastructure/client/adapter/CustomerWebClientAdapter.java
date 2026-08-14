package com.nttdata.bootcamp.account_service.infrastructure.client.adapter;

import com.nttdata.bootcamp.account_service.domain.model.CustomerInfo;
import com.nttdata.bootcamp.account_service.domain.port.output.CustomerClientPort;
import io.reactivex.rxjava3.core.Maybe;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

/**
 * Reactive WebClient adapter implementation for {@link CustomerClientPort} interfacing with customer-service.
 * <p>
 * Technical & Business Rules:
 * - Implements Hexagonal Architecture secondary output port for HTTP microservice communication.
 * - Executes non-blocking reactive HTTP GET requests to retrieve customer type/profile.
 * - Resolves service URLs dynamically via Eureka Service Discovery (http://customer-service).
 * - Handles HTTP 404 Not Found and network connection exceptions gracefully without breaking reactive pipelines.
 * - Bridges Reactor (WebClient) responses to RxJava 3 via {@link RxJava3Adapter}.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerWebClientAdapter implements CustomerClientPort {

    private final WebClient.Builder webClientBuilder;

    private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

    /**
     * Retrieves the customer information required to enforce account holding limits.
     *
     * @param customerId Unique customer primary database identifier.
     * @return A {@link Maybe} emitting the {@link CustomerInfo}, or empty if not found.
     */
    @Override
    public Maybe<CustomerInfo> getById(String customerId) {
        log.debug("Initiating HTTP GET request for customer ID: {}", customerId);

        Mono<CustomerInfo> call = webClientBuilder.build()
                .get()
                .uri("http://customer-service/api/v1/customers/{id}", customerId)
                .retrieve()
                .bodyToMono(CustomerInfo.class)
                .timeout(Duration.ofSeconds(2));

        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("customer-service");

        Mono<CustomerInfo> result = circuitBreaker.run(call, throwable -> {
            if (throwable instanceof WebClientResponseException.NotFound) {
                log.warn("Customer ID '{}' not found in customer-service", customerId);
            } else {
                log.error("Communication error for customer ID '{}': {}",
                        customerId, throwable.getMessage());
            }
            return Mono.empty();
        });

        return RxJava3Adapter.monoToMaybe(result);
    }
}
