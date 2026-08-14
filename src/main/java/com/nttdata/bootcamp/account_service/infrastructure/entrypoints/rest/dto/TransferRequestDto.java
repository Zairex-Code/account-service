package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Data Transfer Object representing the incoming HTTP payload for an account-to-account transfer.
 *
 * @param destinationAccountId Unique primary key identifier of the receiving account.
 * @param amount               Positive monetary amount to transfer.
 */
public record TransferRequestDto(

        @NotBlank(message = "Destination account ID is required and cannot be blank")
        String destinationAccountId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Double amount
) {
}
