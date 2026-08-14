package com.nttdata.bootcamp.account_service.domain.port.output;

import com.nttdata.bootcamp.account_service.domain.model.CustomerInfo;
import io.reactivex.rxjava3.core.Maybe;


/**
 * Secondary output Port interface defining contracts for external customer microservice
 *
 * Technical & Business Rules:
 * - Pure Java domain interface decoupled from WebClient, Feign or HTTP framework dependencies
 * - Enables account-service to retrieve customer type/profile to enforce holding limits.
 * - Enforces non-blocking reactive communication between microservices
 */
public interface CustomerClientPort {

    /**
     * Retrieves the customer information required to enforce account holding limits.
     *
     * @param customerId Unique customer primary database identifier
     * @return A Maybe emitting the CustomerInfo, or empty if the customer does not exist.
     */
    Maybe<CustomerInfo> getById(String customerId);
}
