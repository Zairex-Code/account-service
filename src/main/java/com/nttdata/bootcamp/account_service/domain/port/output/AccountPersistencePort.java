package com.nttdata.bootcamp.account_service.domain.port.output;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Secondary Output Port interface defining reactive database persistence operations
 *
 * Technical & Business Rules:
 * - Pure Java domain interface decoupled from Spring Data, MongoDB, or driver implementation details.
 * - Enforces reactive, non-blocking asynchronous interaction with the underlying data store.
 * - Serves as the SPI (Service Provider Interface) implemented by infrastructure persistence adapters.
 */
public interface AccountPersistencePort {

    /**
     * Persists a new bank account or updates an existing instance in core storage
     *
     * @param account Domain entity instance containing attributes to save
     * @return A Mono  emitting the saved Account domain entity
     */
    Mono<Account> save(Account account);



    /**
     * Retrieves an account entity by its internal primary database identifier
     *
     * @param id unique internal account database identifier.
     * @return A Mono emitting the matching Account domain entity if not found.
     */
    Mono<Account> findById(String id);


    /**
     * Retrieves an account entity by its unique public bank account number
     *
     * @param accountNumber Official unique bank account number
     * @return A Mono emitting the matching Account domain entity
     */
    Mono<Account> findByAccountNumber(String accountNumber);


    /**
     * Retrieves all accounts registered under a specific customer identifier
     *
     * @param customerId Unique customer database identifier
     * @return A Flux streaming all matching Account entities.
     */
    Flux<Account> findByCustomerId(String customerId);


    /**
     * Streams all registered bank accounts stored in the core database
     *
     * @return A Flux streaming all Account entities available in storage.
     */
    Flux<Account> findAll();


    /**
     * Deletes an account entity record from storage by its internal identifier.
     *
     * @param id Unique internal account database identifier.
     * @return A Mono emitting void upon successful deletion
     */
    Mono<Void> deleteById(String id);

}


