package com.nttdata.bootcamp.account_service.domain.port.output;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.time.LocalDateTime;

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
     * @return A Single emitting the saved Account domain entity
     */
    Single<Account> save(Account account);

    /**
     * Retrieves an account entity by its internal primary database identifier
     *
     * @param id unique internal account database identifier.
     * @return A Maybe emitting the matching Account domain entity or empty if not found.
     */
    Maybe<Account> findById(String id);

    /**
     * Retrieves an account entity by its unique public bank account number
     *
     * @param accountNumber Official unique bank account number
     * @return A Maybe emitting the matching Account domain entity or empty if not found.
     */
    Maybe<Account> findByAccountNumber(String accountNumber);

    /**
     * Retrieves all accounts registered under a specific customer identifier
     *
     * @param customerId Unique customer database identifier
     * @return A Flowable streaming all matching Account entities.
     */
    Flowable<Account> findByCustomerId(String customerId);

    /**
     * Retrieves all accounts of a given type registered under a specific customer.
     *
     * @param customerId Unique customer database identifier.
     * @param type       Account product type used to enforce holding limits.
     * @return A Flowable streaming all matching Account entities.
     */
    Flowable<Account> findByCustomerIdAndType(String customerId, AccountType type);

    /**
     * Streams all accounts of a specific customer opened within an inclusive date range.
     *
     * @param customerId Unique customer database identifier.
     * @param start      Inclusive start of the creation date range.
     * @param end        Inclusive end of the creation date range.
     * @return A Flowable streaming all matching Account entities.
     */
    Flowable<Account> findByCustomerIdAndDateRange(String customerId, LocalDateTime start, LocalDateTime end);

    /**
     * Streams all registered bank accounts stored in the core database
     *
     * @return A Flowable streaming all Account entities available in storage.
     */
    Flowable<Account> findAll();

    /**
     * Deletes an account entity record from storage by its internal identifier.
     *
     * @param id Unique internal account database identifier.
     * @return A Completable that completes upon successful deletion
     */
    Completable deleteById(String id);
}
