package com.nttdata.bootcamp.account_service.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Pure Java Domain representing a debit card linked to a bank account.
 * <p>
 * Technical & Business Rules:
 * - Pure POJO decoupled from Spring Framework, MongoDB, or transport layers.
 * - Immutable state using Lombok Builder with 'toBuilder = true'.
 * - A debit card is always linked to a single bank account.
 * </p>
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DebitCard {

    private final String id;
    private final String cardNumber;
    private final String accountId;
    private final DebitCardStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * Transitions the debit card status to BLOCKED.
     *
     * @return A new immutable DebitCard instance in BLOCKED state.
     */
    public DebitCard block() {
        return this.toBuilder()
                .status(DebitCardStatus.BLOCKED)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
