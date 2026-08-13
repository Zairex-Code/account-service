package com.nttdata.bootcamp.account_service.domain.port.output;

import reactor.core.publisher.Mono;


/**
 * Secondary output Port interface defining contracts for external customer microservice
 *
 * Technical & Business Rules:
 * - Pure Java domain interface decoupled from WebClient, Feign or HTTP framework dependencies
 * - Enables account-service to validate customer existence in customer-service prior toaccount creation.
 * - Enforces non-blocking reactive communication between microservices
 */
public interface CustomerClientPort {

    /**
     * Verifies whether a customer exists within the core customer microservice system.
     *
     * @param customerId Unique customer primary database identifier
     * @return A Mono emitting true if the customer exists; false otherwise.
     */
    Mono<Boolean> existsById(String customerId);
}
