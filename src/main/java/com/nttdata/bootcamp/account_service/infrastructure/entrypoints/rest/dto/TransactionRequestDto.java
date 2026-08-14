package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Data Transfer Object representing the incoming HTTP payload for a monetary transaction.
 *
 * @param amount Positive monetary amount to deposit or withdraw.
 */
public record TransactionRequestDto(

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Double amount
) {
}
