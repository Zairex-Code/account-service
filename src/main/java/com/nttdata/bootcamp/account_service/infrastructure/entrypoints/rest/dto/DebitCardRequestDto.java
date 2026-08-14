package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object representing the incoming HTTP payload for debit card issuance.
 *
 * @param accountId Unique primary key identifier of the owning account.
 */
public record DebitCardRequestDto(

        @NotBlank(message = "Account ID is required and cannot be blank")
        String accountId
) {
}
