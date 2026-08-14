package com.nttdata.bootcamp.account_service.application.usecase;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountStatus;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import com.nttdata.bootcamp.account_service.domain.model.CustomerInfo;
import com.nttdata.bootcamp.account_service.domain.model.CustomerType;
import com.nttdata.bootcamp.account_service.domain.port.input.CreateAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.CustomerClientPort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Primary implementation of the {@link CreateAccountUseCase} input port.
 * <p>
 * Technical & Business Rules:
 * - Validates customer existence and type via external customer-service using {@link CustomerClientPort}.
 * - Enforces product holding limits per customer type (Personal vs Business).
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
     * @return A {@link Single} emitting the persisted {@link Account} domain entity.
     * @throws IllegalArgumentException If domain validation fails or customer does not exist.
     * @throws IllegalStateException    If the customer violates account holding limits.
     */
    @Override
    public Single<Account> execute(Account account) {
        log.info("Starting account opening workflow for customer ID: {}",
                account == null ? "null" : account.getCustomerId());

        return validateDomainRules(account)
                .flatMap(validAccount -> customerClientPort.getById(validAccount.getCustomerId())
                        .switchIfEmpty(Single.error(new IllegalArgumentException(
                                "Cannot open account: Customer ID '" + validAccount.getCustomerId()
                                        + "' does not exist")))
                        .flatMap(customerInfo -> enforceHoldingLimits(validAccount, customerInfo)))
                .map(this::prepareAccountForCreation)
                .flatMap(accountPersistencePort::save)
                .doOnSuccess(savedAccount -> log.info("Account opened successfully. ID: {}, Account Number: {}",
                        savedAccount.getId(), savedAccount.getAccountNumber()))
                .doOnError(error -> log.error("Failed to open account for customer ID: {}. Error: {}",
                        account == null ? "null" : account.getCustomerId(), error.getMessage()));
    }

    /**
     * Validates domain constraints on incoming parameters prior to network or database I/O.
     *
     * @param account Incoming account instance.
     * @return A {@link Single} containing the validated account entity.
     */
    private Single<Account> validateDomainRules(Account account) {
        if (account == null) {
            return Single.error(new IllegalArgumentException("Account payload cannot be null"));
        }
        if (account.getCustomerId() == null || account.getCustomerId().isBlank()) {
            return Single.error(new IllegalArgumentException("Customer ID is required account creation"));
        }
        if (account.getType() == null) {
            return Single.error(new IllegalArgumentException(
                    "Account type (SAVING, CURRENT, FIXED_TERM) is required"));
        }
        if (account.getBalance() == null || account.getBalance() < 0) {
            return Single.error(new IllegalArgumentException("Initial balance cannot be null or negative"));
        }
        return Single.just(account);
    }

    /**
     * Enforces account product holding limits based on the customer segment.
     * <p>
     * Business Rules:
     * - Personal: maximum one SAVINGS, one CURRENT, unlimited FIXED_TERM.
     * - Business: multiple CURRENT, no SAVINGS and no FIXED_TERM.
     * </p>
     *
     * @param account      Incoming account instance.
     * @param customerInfo Customer segment context retrieved from customer-service.
     * @return A {@link Single} containing the validated account entity.
     */
    private Single<Account> enforceHoldingLimits(Account account, CustomerInfo customerInfo) {
        AccountType requestedType = account.getType();

        if (customerInfo.type() == CustomerType.PERSONAL) {
            if (requestedType == AccountType.FIXED_TERM) {
                return Single.just(account);
            }
            return accountPersistencePort
                    .findByCustomerIdAndType(account.getCustomerId(), requestedType)
                    .count()
                    .flatMap(count -> {
                        if (count > 0) {
                            return Single.error(new IllegalStateException(
                                    "Customer already holds a " + requestedType
                                            + " account. Only one is allowed."));
                        }
                        return Single.just(account);
                    });
        }

        if (requestedType == AccountType.SAVINGS || requestedType == AccountType.FIXED_TERM) {
            return Single.error(new IllegalStateException(
                    "Business customers cannot acquire " + requestedType + " accounts"));
        }
        return Single.just(account);
    }

    /**
     * Constructs a fully initialized, immutable {@link Account} domain entity ready for storage.
     *
     * @param account Base account domain request.
     * @return Fully populated immutable {@link Account} instance.
     */
    private Account prepareAccountForCreation(Account account) {

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
    private String generateAccountNumber() {
        long randomDigits = Math.abs(UUID.randomUUID().getMostSignificantBits()) % 10000000000L;
        return String.format("191-%010d", randomDigits);
    }
}
