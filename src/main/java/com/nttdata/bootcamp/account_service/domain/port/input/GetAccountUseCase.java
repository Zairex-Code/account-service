package com.nttdata.bootcamp.account_service.domain.port.input;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * Input Port interface defining read contracts for querying bank account details
 *
 * Technical & Business Rules:
 * - Pure Java domain interface decoupled for WebFlux, Spring or persistence layers
 * - Supports reactive non-blocking retrieval by unique ID account number or customer
 * - Streams multiple account records for customers holding multiple financial products
 *
 */
public interface GetAccountUseCase {

    /**
     * Retrieves a single bank account by its internal primary database identifier
     *
     * @param id Unique internal account database identifier
     * @return A Mono emitting the matching Account domain entity, or empty if not found
     */
    Mono<Account> findById(String id);

    /**
     * Retrieves a single bank account by its official public account number
     *
     * @param accountNumber  Official unique 14 to 20 digit bank account number
     * @return A Mono emitting the matching Account domain entity or empty if not found.
     */
    Mono<Account> findByAccountNumber(String accountNumber);

    /**
     * Retrieves all bank accounts belonging to a specific customer
     *
     * @param customerId Unique internal database identifier of the customer
     * @return A Flux streaming all Account entities associated with the customer.
     */
    Flux<Account> findByCustomerId(String customerId);


    /**
     * Streams all registered bank accounts stored within the financial core
     *
     * @return A Flux streaming all Account entities available in persistence core
     */
    Flux<Account> findAll();
}
