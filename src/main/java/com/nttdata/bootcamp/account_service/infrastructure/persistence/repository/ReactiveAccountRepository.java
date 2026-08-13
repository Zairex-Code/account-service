package com.nttdata.bootcamp.account_service.infrastructure.persistence.repository;

import com.nttdata.bootcamp.account_service.infrastructure.persistence.document.AccountDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
/**
 * Reactive Spring Data MongoDB repository interface for the 'accounts' collection.
 * <p>
 * Technical & Business Rules:
 * - Inherits non-blocking reactive CRUD operations from {@link ReactiveMongoRepository}.
 * - Leverages Spring Data derived query methods to eliminate manual DB queries.
 * - Complies with project guidelines prohibiting raw @Query annotations.
 * - Operates strictly on {@link AccountDocument} persistence entities.
 * </p>
 */
@Repository
public interface ReactiveAccountRepository extends ReactiveMongoRepository<AccountDocument, String> {
    /**
     * Retrieves an account document by its unique 14-digit public bank account number.
     *
     * @param accountNumber Unique public bank account number.
     * @return A {@link Mono} emitting the matching {@link AccountDocument}, or empty if not found.
     */
    Mono<AccountDocument> findByAccountNumber(String accountNumber);

    /**
     * Streams all account documents registered under a specific customer identifier.
     *
     * @param customerId Unique customer primary database identifier.
     * @return A {@link Flux} streaming all matching {@link AccountDocument} entities.
     */
    Flux<AccountDocument> findByCustomerId(String customerId);
}
