package com.nttdata.bootcamp.account_service.application.usecase;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.DebitCardStatus;
import com.nttdata.bootcamp.account_service.domain.port.input.PayWithDebitCardUseCase;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.DebitCardPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.MovementClientPort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementation of the {@link PayWithDebitCardUseCase} input port.
 * <p>
 * Technical & Business Rules:
 * - Validates the debit card exists and is ACTIVE.
 * - Debits the linked account via the rich domain method {@link Account#withdraw}.
 * - Records the movement in the ledger.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayWithDebitCardUseCaseImpl implements PayWithDebitCardUseCase {

    private final DebitCardPersistencePort debitCardPersistencePort;
    private final AccountPersistencePort accountPersistencePort;
    private final MovementClientPort movementClientPort;

    @Override
    public Single<Account> pay(String cardId, Double amount) {
        log.info("Initiating debit card payment for card ID: {}", cardId);

        if (cardId == null || cardId.isBlank()) {
            return Single.error(new IllegalArgumentException(
                    "Card ID cannot be null or blank for payment"));
        }
        if (amount == null || amount <= 0) {
            return Single.error(new IllegalArgumentException(
                    "Payment amount must be greater than zero"));
        }

        return debitCardPersistencePort.findById(cardId)
                .switchIfEmpty(Single.error(new IllegalArgumentException(
                        "Cannot pay: Debit card with ID '" + cardId + "' was not found")))
                .flatMap(card -> {
                    if (card.getStatus() != DebitCardStatus.ACTIVE) {
                        return Single.error(new IllegalStateException(
                                "Cannot pay: Debit card is not ACTIVE"));
                    }
                    return accountPersistencePort.findById(card.getAccountId())
                            .switchIfEmpty(Single.error(new IllegalArgumentException(
                                    "Cannot pay: Linked account with ID '" + card.getAccountId()
                                            + "' was not found")))
                            .map(account -> account.withdraw(amount))
                            .flatMap(accountPersistencePort::save)
                            .flatMap(saved -> movementClientPort
                                    .recordMovement(saved.getId(), "ACCOUNT", "WITHDRAWAL", amount)
                                    .andThen(Single.just(saved)));
                })
                .doOnSuccess(account -> log.info(
                        "Debit card payment applied. Account ID '{}'. New balance: {}",
                        account.getId(), account.getBalance()))
                .doOnError(error -> log.error(
                        "Failed debit card payment for card ID '{}'. Error: {}",
                        cardId, error.getMessage()));
    }
}
