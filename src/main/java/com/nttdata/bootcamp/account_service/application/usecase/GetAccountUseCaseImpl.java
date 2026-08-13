package com.nttdata.bootcamp.account_service.application.usecase;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.port.input.GetAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * Service implementation of the {@link GetAccountUseCase} input port.
 * <p>
 * Technical & Business Rules:
 * - Delegates reactive query operations to the output persistence port {@link AccountPersistencePort}.
 * - Emits log traces for non-blocking audit tracking during read operations.
 * - Guarantees non-blocking execution using WebFlux reactive streams.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetAccountUseCaseImpl implements GetAccountUseCase {

    private final AccountPersistencePort accountPersistencePort;



    /**
     * Retrieves a single bank account by its internal primary database identifier
     *
     * @param id Unique internal account database identifier
     * @return A Mono emitting the matching Account domain entity, or empty if not found
     */
    @Override
    public Mono<Account> findById(String id) {
        log.debug("Querying account by ID: {}", id);
        return accountPersistencePort.findById(id)
                .doOnSuccess(account -> {
                    if (account != null){
                        log.debug("Account found with ID: {}", id);
                    }else {
                        log.warn("Account not found with ID: {}", id);
                    }
                });

    }

    /**
     * Retrieves a single bank account by its official public account number
     *
     * @param accountNumber Official unique 14 to 20 digit bank account number
     * @return A Mono emitting the matching Account domain entity or empty if not found.
     */
    @Override
    public Mono<Account> findByAccountNumber(String accountNumber) {
        log.debug("Querying account by Account Number: {}", accountNumber);
        return accountPersistencePort.findByAccountNumber(accountNumber)
                .doOnSuccess(account -> {
                    if (account != null){
                        log.debug("Account found with Account Number: {}", accountNumber);
                    }else {
                        log.debug("Account not found with Account Number: {}", accountNumber);
                    }

        });
    }

    /**
     * Retrieves all bank accounts belonging to a specific customer
     *
     * @param customerId Unique internal database identifier of the customer
     * @return A Flux streaming all Account entities associated with the customer.
     */
    @Override
    public Flux<Account> findByCustomerId(String customerId) {
        log.debug("Streaming accounts for Customer ID: {}", customerId);
        return accountPersistencePort.findByCustomerId(customerId)
                .doOnComplete(() -> log.debug("Completed streaming accounts for Customer ID: {}", customerId));
    }

    /**
     * Streams all registered bank accounts stored within the financial core
     *
     * @return A Flux streaming all Account entities available in persistence core
     */
    @Override
    public Flux<Account> findAll() {
        log.debug("Streaming all accounts stored in financial core");
        return accountPersistencePort.findAll()
                .doOnComplete(() -> log.debug("Completed streaming all accounts"));
    }
}
