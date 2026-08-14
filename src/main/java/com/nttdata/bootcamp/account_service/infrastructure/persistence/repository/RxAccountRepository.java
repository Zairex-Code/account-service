package com.nttdata.bootcamp.account_service.infrastructure.persistence.repository;

import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import com.nttdata.bootcamp.account_service.infrastructure.persistence.document.AccountDocument;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import java.time.LocalDateTime;
import org.springframework.data.repository.reactive.RxJava3CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Reactive Spring Data MongoDB repository interface for the 'accounts' collection.
 * <p>
 * Technical & Business Rules:
 * - Inherits non-blocking reactive CRUD operations from {@link RxJava3CrudRepository}.
 * - Leverages Spring Data derived query methods to eliminate manual DB queries.
 * - Complies with project guidelines prohibiting raw @Query annotations.
 * - Operates strictly on {@link AccountDocument} persistence entities.
 * </p>
 */
@Repository
public interface RxAccountRepository extends RxJava3CrudRepository<AccountDocument, String> {
    /**
     * Retrieves an account document by its unique 14-digit public bank account number.
     *
     * @param accountNumber Unique public bank account number.
     * @return A {@link Maybe} emitting the matching {@link AccountDocument}, or empty if not found.
     */
    Maybe<AccountDocument> findByAccountNumber(String accountNumber);

    /**
     * Streams all account documents registered under a specific customer identifier.
     *
     * @param customerId Unique customer primary database identifier.
     * @return A {@link Flowable} streaming all matching {@link AccountDocument} entities.
     */
    Flowable<AccountDocument> findByCustomerId(String customerId);

    /**
     * Streams all account documents of a given type registered under a specific customer.
     *
     * @param customerId Unique customer primary database identifier.
     * @param type       Account product type used to enforce holding limits.
     * @return A {@link Flowable} streaming all matching {@link AccountDocument} entities.
     */
    Flowable<AccountDocument> findByCustomerIdAndType(String customerId, AccountType type);

    /**
     * Streams all account documents registered under a specific customer opened within a date range.
     *
     * @param customerId Unique customer primary database identifier.
     * @param start      Inclusive start of the creation date range.
     * @param end        Inclusive end of the creation date range.
     * @return A {@link Flowable} streaming all matching {@link AccountDocument} entities.
     */
    Flowable<AccountDocument> findByCustomerIdAndCreatedAtBetween(String customerId,
                                                                  LocalDateTime start,
                                                                  LocalDateTime end);
}
