package com.nttdata.bootcamp.account_service.application.usecase;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.port.input.DepositAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.MovementClientPort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation of the {@link DepositAccountUseCase} input port.
 * <p>
 * Technical & Business Rules:
 * - Validates existing account presence and deposit amount prior to processing.
 * - Delegates balance crediting to the rich domain method {@link Account#deposit}.
 * - Persists the updated account state after the deposit.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepositAccountUseCaseImpl implements DepositAccountUseCase {

    private final AccountPersistencePort accountPersistencePort;
    private final MovementClientPort movementClientPort;

    /**
     * Deposits a positive monetary amount into an existing bank account.
     *
     * @param id     Unique primary database identifier of the account.
     * @param amount Positive monetary amount to deposit.
     * @return A Single emitting the updated Account domain entity.
     */
    @Override
    public Single<Account> deposit(String id, Double amount) {
        log.info("Initiating deposit into account ID: {}", id);

        if (id == null || id.isBlank()) {
            return Single.error(new IllegalArgumentException(
                    "Account ID cannot be null or blank for deposit"));
        }
        if (amount == null || amount <= 0) {
            return Single.error(new IllegalArgumentException(
                    "Deposit amount must be strictly greater than zero"));
        }

        return accountPersistencePort.findById(id)
                .switchIfEmpty(Single.error(new IllegalArgumentException(
                        "Cannot deposit: Account with ID '" + id + "' was not found")))
                .map(account -> account.deposit(amount))
                .flatMap(accountPersistencePort::save)
                .flatMap(saved -> movementClientPort.recordMovement(id, "ACCOUNT", "DEPOSIT", amount)
                        .andThen(Single.just(saved)))
                .doOnSuccess(account -> log.info(
                        "Deposit applied to account ID '{}'. New balance: {}",
                        id, account.getBalance()))
                .doOnError(error -> log.error(
                        "Failed to deposit into account ID '{}'. Error: {}", id, error.getMessage()));
    }
}
