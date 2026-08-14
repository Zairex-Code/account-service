package com.nttdata.bootcamp.account_service.application.usecase;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.port.input.WithdrawAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.MovementClientPort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation of the {@link WithdrawAccountUseCase} input port.
 * <p>
 * Technical & Business Rules:
 * - Validates existing account presence and withdrawal amount prior to processing.
 * - Delegates balance debiting to the rich domain method {@link Account#withdraw}.
 * - Persists the updated account state after the withdrawal.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawAccountUseCaseImpl implements WithdrawAccountUseCase {

    private final AccountPersistencePort accountPersistencePort;
    private final MovementClientPort movementClientPort;

    /**
     * Withdraws a positive monetary amount from an existing bank account.
     *
     * @param id     Unique primary database identifier of the account.
     * @param amount Positive monetary amount to withdraw.
     * @return A Single emitting the updated Account domain entity.
     */
    @Override
    public Single<Account> withdraw(String id, Double amount) {
        log.info("Initiating withdrawal from account ID: {}", id);

        if (id == null || id.isBlank()) {
            return Single.error(new IllegalArgumentException(
                    "Account ID cannot be null or blank for withdrawal"));
        }
        if (amount == null || amount <= 0) {
            return Single.error(new IllegalArgumentException(
                    "Withdrawal amount must be strictly greater than zero"));
        }

        return accountPersistencePort.findById(id)
                .switchIfEmpty(Single.error(new IllegalArgumentException(
                        "Cannot withdraw: Account with ID '" + id + "' was not found")))
                .map(account -> account.withdraw(amount))
                .flatMap(accountPersistencePort::save)
                .flatMap(saved -> movementClientPort.recordMovement(id, "ACCOUNT", "WITHDRAWAL", amount)
                        .andThen(Single.just(saved)))
                .doOnSuccess(account -> log.info(
                        "Withdrawal applied to account ID '{}'. New balance: {}",
                        id, account.getBalance()))
                .doOnError(error -> log.error(
                        "Failed to withdraw from account ID '{}'. Error: {}", id, error.getMessage()));
    }
}
