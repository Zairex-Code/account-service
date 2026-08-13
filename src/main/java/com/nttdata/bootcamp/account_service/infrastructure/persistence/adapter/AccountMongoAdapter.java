package com.nttdata.bootcamp.account_service.infrastructure.persistence.adapter;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.infrastructure.persistence.mapper.AccountPersistenceMapper;
import com.nttdata.bootcamp.account_service.infrastructure.persistence.repository.ReactiveAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Persistence adapter implementation for {@link AccountPersistencePort} interfacing with MongoDB.
 * <p>
 * Technical & Business Rules:
 * - Implements Hexagonal Architecture secondary output port for bank account storage.
 * - Uses {@link AccountPersistenceMapper} to convert between domain entities and MongoDB documents.
 * - Leverages non-blocking reactive driver operations via {@link ReactiveAccountRepository}.
 * - Registered as a Spring {@link Component} to fulfill dependency injection for application use cases.
 * </p>
 *
 * @author NTT DATA Bootcamp Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountMongoAdapter implements AccountPersistencePort {

    private final ReactiveAccountRepository repository;
    private final AccountPersistenceMapper mapper;

    /**
     * Persists or updates an account entity in MongoDB storage.
     *
     * @param account Domain model instance containing state to persist.
     * @return A {@link Mono} emitting the saved {@link Account} domain entity.
     */
    @Override
    public Mono<Account> save(Account account) {
        log.debug("Adapting domain Account to Document for persistence. Customer ID: {}", account.getCustomerId());
        return Mono.just(account)
                .map(mapper::toDocument)
                .flatMap(repository::save)
                .map(mapper::toDomain)
                .doOnSuccess(savedAccount -> log.debug("Successfully saved account ID: {}", savedAccount.getId()));
    }

    /**
     * Retrieves an account entity by its internal database primary key.
     *
     * @param id Unique MongoDB document primary key string.
     * @return A {@link Mono} emitting the matching {@link Account} domain entity, or empty if not found.
     */
    @Override
    public Mono<Account> findById(String id) {
        log.debug("Finding account document in MongoDB by ID: {}", id);
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    /**
     * Retrieves an account entity by its unique 14-digit public account number.
     *
     * @param accountNumber Unique public bank account number.
     * @return A {@link Mono} emitting the matching {@link Account} domain entity, or empty if not found.
     */
    @Override
    public Mono<Account> findByAccountNumber(String accountNumber) {
        log.debug("Finding account document in MongoDB by account number: {}", accountNumber);
        return repository.findByAccountNumber(accountNumber)
                .map(mapper::toDomain);
    }

    /**
     * Streams all account entities associated with a specific customer identifier.
     *
     * @param customerId Unique customer database primary key string.
     * @return A {@link Flux} streaming matching {@link Account} domain entities.
     */
    @Override
    public Flux<Account> findByCustomerId(String customerId) {
        log.debug("Streaming account documents from MongoDB for customer ID: {}", customerId);
        return repository.findByCustomerId(customerId)
                .map(mapper::toDomain);
    }

    /**
     * Streams all registered account entities stored in MongoDB.
     *
     * @return A {@link Flux} streaming all available {@link Account} domain entities.
     */
    @Override
    public Flux<Account> findAll() {
        log.debug("Streaming all account documents from MongoDB collection");
        return repository.findAll()
                .map(mapper::toDomain);
    }

    /**
     * Deletes an account document from MongoDB by its primary key.
     *
     * @param id Unique MongoDB document primary key string.
     * @return A {@link Mono} emitting void upon successful deletion.
     */
    @Override
    public Mono<Void> deleteById(String id) {
        log.debug("Deleting account document from MongoDB by ID: {}", id);
        return repository.deleteById(id);
    }
}