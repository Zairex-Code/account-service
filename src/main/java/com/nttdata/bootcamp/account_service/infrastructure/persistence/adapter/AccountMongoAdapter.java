package com.nttdata.bootcamp.account_service.infrastructure.persistence.adapter;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.infrastructure.persistence.mapper.AccountPersistenceMapper;
import com.nttdata.bootcamp.account_service.infrastructure.persistence.repository.RxAccountRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Persistence adapter implementation for {@link AccountPersistencePort} interfacing with MongoDB.
 * <p>
 * Technical & Business Rules:
 * - Implements Hexagonal Architecture secondary output port for bank account storage.
 * - Uses {@link AccountPersistenceMapper} to convert between domain entities and MongoDB documents.
 * - Leverages non-blocking reactive driver operations via {@link RxAccountRepository}.
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

    private final RxAccountRepository repository;
    private final AccountPersistenceMapper mapper;

    /**
     * Persists or updates an account entity in MongoDB storage.
     *
     * @param account Domain model instance containing state to persist.
     * @return A {@link Single} emitting the saved {@link Account} domain entity.
     */
    @Override
    public Single<Account> save(Account account) {
        log.debug("Adapting domain Account to Document for persistence. Customer ID: {}", account.getCustomerId());
        return Single.just(account)
                .map(mapper::toDocument)
                .flatMap(repository::save)
                .map(mapper::toDomain)
                .doOnSuccess(savedAccount -> log.debug("Successfully saved account ID: {}", savedAccount.getId()));
    }

    /**
     * Retrieves an account entity by its internal database primary key.
     *
     * @param id Unique MongoDB document primary key string.
     * @return A {@link Maybe} emitting the matching {@link Account} domain entity, or empty if not found.
     */
    @Override
    public Maybe<Account> findById(String id) {
        log.debug("Finding account document in MongoDB by ID: {}", id);
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    /**
     * Retrieves an account entity by its unique 14-digit public account number.
     *
     * @param accountNumber Unique public bank account number.
     * @return A {@link Maybe} emitting the matching {@link Account} domain entity, or empty if not found.
     */
    @Override
    public Maybe<Account> findByAccountNumber(String accountNumber) {
        log.debug("Finding account document in MongoDB by account number: {}", accountNumber);
        return repository.findByAccountNumber(accountNumber)
                .map(mapper::toDomain);
    }

    /**
     * Streams all account entities associated with a specific customer identifier.
     *
     * @param customerId Unique customer database primary key string.
     * @return A {@link Flowable} streaming matching {@link Account} domain entities.
     */
    @Override
    public Flowable<Account> findByCustomerId(String customerId) {
        log.debug("Streaming account documents from MongoDB for customer ID: {}", customerId);
        return repository.findByCustomerId(customerId)
                .map(mapper::toDomain);
    }

    /**
     * Streams all account entities of a given type associated with a specific customer.
     *
     * @param customerId Unique customer database primary key string.
     * @param type       Account product type used to enforce holding limits.
     * @return A {@link Flowable} streaming matching {@link Account} domain entities.
     */
    @Override
    public Flowable<Account> findByCustomerIdAndType(String customerId, AccountType type) {
        log.debug("Streaming account documents from MongoDB for customer ID: {} and type: {}",
                customerId, type);
        return repository.findByCustomerIdAndType(customerId, type)
                .map(mapper::toDomain);
    }

    /**
     * Streams all registered account entities stored in MongoDB.
     *
     * @return A {@link Flowable} streaming all available {@link Account} domain entities.
     */
    @Override
    public Flowable<Account> findAll() {
        log.debug("Streaming all account documents from MongoDB collection");
        return repository.findAll()
                .map(mapper::toDomain);
    }

    /**
     * Deletes an account document from MongoDB by its primary key.
     *
     * @param id Unique MongoDB document primary key string.
     * @return A {@link Completable} that completes upon successful deletion.
     */
    @Override
    public Completable deleteById(String id) {
        log.debug("Deleting account document from MongoDB by ID: {}", id);
        return repository.deleteById(id);
    }
}
