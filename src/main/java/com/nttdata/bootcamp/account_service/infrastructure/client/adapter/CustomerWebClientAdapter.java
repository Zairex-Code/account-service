package com.nttdata.bootcamp.account_service.infrastructure.client.adapter;

import com.nttdata.bootcamp.account_service.domain.port.output.CustomerClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Reactive WebClient adapter implementation for {@link CustomerClientPort} interfacing with customer-service.
 * <p>
 * Technical & Business Rules:
 * - Implements Hexagonal Architecture secondary output port for HTTP microservice communication.
 * - Executes non-blocking reactive HTTP GET requests to validate customer existence in customer-service.
 * - Resolves service URLs dynamically via Eureka Service Discovery (http://customer-service).
 * - Handles HTTP 404 Not Found and network connection exceptions gracefully without breaking reactive pipelines.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerWebClientAdapter implements CustomerClientPort {
    private final WebClient.Builder webClientBuilder;

    /**
     * Verifies whether a customer exists in the core customer-service microservice asynchronously.
     *
     * @param customerId Unique primary database identifier of the customer.
     * @return A {@link Mono} emitting true if customer exists; false if not found or on network error.
     */
    @Override
    public Mono<Boolean> existsById(String customerId) {
        log.debug("Initiating reactive HTTP GET request to verify existence of customer ID: {}", customerId);

        return webClientBuilder.build()
                .get()
                .uri("http://customer-service/api/v1/customers/{id}", customerId)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    log.warn("Customer ID '{}' was not found in customer-service (HTTP 404)", customerId);
                    return Mono.just(false);
                })
                .onErrorResume(ex -> {
                    log.error("Communication error invoking customer-service for ID '{}': {}", customerId, ex.getMessage());
                    return Mono.just(false);
                });
    }
}
