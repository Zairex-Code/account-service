package com.nttdata.bootcamp.account_service.application.usecase;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.port.input.TransferAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.MovementClientPort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation of the {@link TransferAccountUseCase} input port.
 * <p>
 * Technical & Business Rules:
 * - Validates source/destination existence and a positive transfer amount.
 * - Delegates debiting/crediting to the rich domain methods
 *   {@link Account#withdraw} and {@link Account#deposit}.
 * - Persists both affected accounts and records a TRANSFER movement for each side.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferAccountUseCaseImpl implements TransferAccountUseCase {

    private final AccountPersistencePort accountPersistencePort;
    private final MovementClientPort movementClientPort;

    /**
     * Transfers a positive monetary amount between two existing accounts.
     *
     * @param sourceId      Unique primary database identifier of the source account.
     * @param destinationId Unique primary database identifier of the destination account.
     * @param amount        Positive monetary amount to transfer.
     * @return A Single emitting the updated source Account domain entity.
     */
    @Override
    public Single<Account> transfer(String sourceId, String destinationId, Double amount) {
        log.info("Initiating transfer from account ID '{}' to '{}'", sourceId, destinationId);

        if (sourceId == null || sourceId.isBlank()) {
            return Single.error(new IllegalArgumentException(
                    "Source account ID cannot be null or blank"));
        }
        if (destinationId == null || destinationId.isBlank()) {
            return Single.error(new IllegalArgumentException(
                    "Destination account ID cannot be null or blank"));
        }
        if (sourceId.equals(destinationId)) {
            return Single.error(new IllegalArgumentException(
                    "Source and destination accounts must be different"));
        }
        if (amount == null || amount <= 0) {
            return Single.error(new IllegalArgumentException(
                    "Transfer amount must be strictly greater than zero"));
        }

        return accountPersistencePort.findById(sourceId)
                .switchIfEmpty(Single.error(new IllegalArgumentException(
                        "Cannot transfer: source account with ID '" + sourceId + "' was not found")))
                .flatMap(source -> accountPersistencePort.findById(destinationId)
                        .switchIfEmpty(Single.error(new IllegalArgumentException(
                                "Cannot transfer: destination account with ID '"
                                        + destinationId + "' was not found")))
                        .flatMap(destination -> {
                            Account debitedSource = source.withdraw(amount);
                            Account creditedDestination = destination.deposit(amount);

                            return accountPersistencePort.save(debitedSource)
                                    .flatMap(savedSource -> accountPersistencePort.save(creditedDestination)
                                            .flatMap(savedDestination -> movementClientPort
                                                    .recordMovement(sourceId, "ACCOUNT", "TRANSFER", amount)
                                                    .andThen(movementClientPort.recordMovement(
                                                            destinationId, "ACCOUNT", "TRANSFER", amount))
                                                    .andThen(Single.just(savedSource))));
                        }))
                .doOnSuccess(source -> log.info(
                        "Transfer completed from '{}' to '{}'. Source balance: {}",
                        sourceId, destinationId, source.getBalance()))
                .doOnError(error -> log.error(
                        "Transfer from '{}' to '{}' failed. Error: {}",
                        sourceId, destinationId, error.getMessage()));
    }
}
