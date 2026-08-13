package com.nttdata.bootcamp.account_service.application.usecase;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountStatus;
import com.nttdata.bootcamp.account_service.domain.port.input.CreateAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.CustomerClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Primary implementation of the {@link CreateAccountUseCase} input port.
 * <p>
 * Technical & Business Rules:
 * - Validates customer existence via external customer-service using {@link CustomerClientPort}.
 * - Verifies domain business constraints prior to initiating network or database calls.
 * - Auto-generates a unique 14-digit account number (191-XXXXXXXXXX) if absent.
 * - Assigns default operational status (ACTIVE), transaction counter to zero, and audit timestamps.
 * - Persists the newly configured immutable {@link Account} via {@link AccountPersistencePort}.
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAccountUseCaseImpl implements CreateAccountUseCase {

    private final AccountPersistencePort accountPersistencePort;
    private final CustomerClientPort customerClientPort;

    /**
     * Executes the reactive business orchestration for creating a new bank account.
     *
     * @param account Proposed immutable account domain entity.
     * @return A {@link Mono} emitting the persisted {@link Account} domain entity.
     * @throws IllegalArgumentException If domain validation fails or customer does not exist.
     */
    @Override
    public Mono<Account> execute(Account account) {
        log.info("Starting account opening workflow for customer ID: {}", account.getCustomerId());

        return validateDomainRules(account)
                .flatMap(validAccount -> customerClientPort.existsById(validAccount.getCustomerId()))
                .flatMap(exists -> {
                    if (!Boolean.TRUE.equals(exists)) {
                        log.error("Account creation rejected: Customer ID '{}' does not exist", account.getCustomerId());
                        return Mono.error(new IllegalArgumentException(
                                "Cannot open account: Customer ID '" + account.getCustomerId() + "' was not found"));
                    }

                    Account accountToSave = prepareAccountForCreation(account);
                    log.debug("Account entity prepared with generated account number: {}", accountToSave.getAccountNumber());
                    return accountPersistencePort.save(accountToSave);
                })
                .doOnSuccess(savedAccount -> log.info("Account opened successfully. ID: {}, Account Number: {}",
                        savedAccount.getId(), savedAccount.getAccountNumber()))
                .doOnError(error -> log.error("Failed to open account for customer ID: {}. Error: {}",
                        account.getCustomerId(), error.getMessage()));
    }

    /**
     * Validates domain constraints on incoming parameters prior to network or database I/O.
     *
     * @param account Incoming account instance.
     * @return A {@link Mono} containing the validated account entity.
     */
    private Mono<Account> validateDomainRules(Account account){
        if (account == null){
            return Mono.error(new IllegalArgumentException("Account payload cannot be null"));
        }
        if (account.getCustomerId() == null || account.getCustomerId().isBlank()){
            return Mono.error(new IllegalArgumentException("Customer ID is required account creation"));
        }
        if (account.getType() == null){
            return Mono.error(new IllegalArgumentException("Account type (SAVING, CURRENT, FIXED_TERM) is required"));
        }
        if (account.getBalance() == null || account.getBalance() < 0){
            return Mono.error(new IllegalArgumentException("Initial balance cannot be null or negative"));
        }
        return Mono.just(account);
    }



    /**
     * Constructs a fully initialized, immutable {@link Account} domain entity ready for storage.
     *
     * @param account Base account domain request.
     * @return Fully populated immutable {@link Account} instance.
     */
    private Account prepareAccountForCreation(Account account){

        String accountNumber = (account.getAccountNumber() != null && !account.getAccountNumber().isBlank())
                ? account.getAccountNumber()
                : generateAccountNumber();

        return account.toBuilder()
                .accountNumber(accountNumber)
                .status(AccountStatus.ACTIVE)
                .currentMonthlyTransactions(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Generates a unique 14-digit bank account number using standard BCP prefix format.
     *
     * @return Formatted 14-digit numeric string (e.g., "191-0048291042").
     */
    private String generateAccountNumber(){
        long randomDigits = Math.abs(UUID.randomUUID().getMostSignificantBits()) % 10000000000L;
        return String.format("191-%010d", randomDigits);
    }
}
