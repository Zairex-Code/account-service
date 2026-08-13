package com.nttdata.bootcamp.account_service.application.usecase;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.port.input.UpdateAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Service implementation of the {@link UpdateAccountUseCase} input port.
 * <p>
 * Technical & Business Rules :
 * - Validates existing account presence prior to updating.
 * - Protects immutable identity and audit attributes (id, accountNumber, customerId, createdAt).
 * - Merges modifiable business attributes (status, maintenanceFee, maxMonthlyTransactions, holders, signatories).
 * - Assigns an updated audit timestamp (updatedAt) and persists via {@link AccountPersistencePort}.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateAccountUseCaseImpl implements UpdateAccountUseCase {

    private final AccountPersistencePort accountPersistencePort;

    /**
     * Evaluates domain business rules and updates an existing bank account asynchronously
     *
     * @param id      Unique primary database identifier of the account to update
     * @param account account Domain entity instance containing updated attribute value
     * @return A Mono emitting the updated Account domain entity
     */
    @Override
    public Mono<Account> execute(String id, Account account) {
        log.info("Initiating account update process for account ID: {}", id);

        if (id == null || id.isBlank()){
            return Mono.error(new IllegalArgumentException("Account ID cannot be null or blank for update"));
        }

        if (account == null){
            return Mono.error(new IllegalArgumentException("Update account payload cannot be null"));
        }

        return accountPersistencePort.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cannot update account: Account with ID '"+id+"' was not found")))
                .flatMap(existingAccount -> {
                    Account updatedAccount = mergeAccountFields(existingAccount, account);
                    log.debug("Account ID '{}' merged with new state. status. {}", id,updatedAccount.getStatus());
                    return accountPersistencePort.save(updatedAccount);
                })
                .doOnSuccess(savedAccount -> log.debug("Account ID '{}' updated successfully", savedAccount.getId()))
                .doOnError(error -> log.error("Failed to update account ID '{}'. Error: {}", id, error.getMessage()));
    }

    /**
     * Merges updateable domain fields into the existing immutable account instance.
     *
     * @param existing Original persisted account state.
     * @param incoming Proposed account modifications.
     * @return A new immutable {@link Account} instance with updated properties.
     */
    private Account mergeAccountFields(Account existing, Account incoming) {
        return existing.toBuilder()
                .status(incoming.getStatus() != null ? incoming.getStatus() : existing.getStatus())
                .maintenanceFee(incoming.getMaintenanceFee() != null ? incoming.getMaintenanceFee() : existing.getMaintenanceFee())
                .maxMonthlyTransactions(incoming.getMaxMonthlyTransactions() != null ? incoming.getMaxMonthlyTransactions() : existing.getMaxMonthlyTransactions())
                .allowedTransactionDay(incoming.getAllowedTransactionDay() != null ? incoming.getAllowedTransactionDay() : existing.getAllowedTransactionDay())
                .holders(incoming.getHolders() != null && !incoming.getHolders().isEmpty() ? incoming.getHolders() : existing.getHolders())
                .signatories(incoming.getSignatories() != null && !incoming.getSignatories().isEmpty() ? incoming.getSignatories() : existing.getSignatories())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
