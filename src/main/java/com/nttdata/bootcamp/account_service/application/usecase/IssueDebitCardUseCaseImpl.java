package com.nttdata.bootcamp.account_service.application.usecase;

import com.nttdata.bootcamp.account_service.domain.model.DebitCard;
import com.nttdata.bootcamp.account_service.domain.model.DebitCardStatus;
import com.nttdata.bootcamp.account_service.domain.port.input.IssueDebitCardUseCase;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.DebitCardPersistencePort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service implementation of the {@link IssueDebitCardUseCase} input port.
 * <p>
 * Technical & Business Rules:
 * - Validates the owning account exists prior to issuing a card.
 * - Generates a unique 16-digit card number.
 * - Persists the new card in ACTIVE status.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueDebitCardUseCaseImpl implements IssueDebitCardUseCase {

    private final AccountPersistencePort accountPersistencePort;
    private final DebitCardPersistencePort debitCardPersistencePort;

    @Override
    public Single<DebitCard> issue(String accountId) {
        log.info("Initiating debit card issuance for account ID: {}", accountId);

        if (accountId == null || accountId.isBlank()) {
            return Single.error(new IllegalArgumentException(
                    "Account ID cannot be null or blank for debit card issuance"));
        }

        return accountPersistencePort.findById(accountId)
                .switchIfEmpty(Single.error(new IllegalArgumentException(
                        "Cannot issue debit card: Account with ID '" + accountId + "' was not found")))
                .flatMap(account -> {
                    LocalDateTime now = LocalDateTime.now();
                    DebitCard card = DebitCard.builder()
                            .cardNumber(generateCardNumber())
                            .accountId(accountId)
                            .status(DebitCardStatus.ACTIVE)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return debitCardPersistencePort.save(card);
                })
                .doOnSuccess(card -> log.info("Debit card issued. ID: {}, Number: {}",
                        card.getId(), card.getCardNumber()))
                .doOnError(error -> log.error("Failed to issue debit card for account ID '{}'. Error: {}",
                        accountId, error.getMessage()));
    }

    private String generateCardNumber() {
        long random = Math.abs(ThreadLocalRandom.current().nextLong()) % 1000000000000L;
        return String.format("4500-%04d-%04d-%04d",
                random / 100000000, (random / 10000) % 10000, random % 10000);
    }
}
