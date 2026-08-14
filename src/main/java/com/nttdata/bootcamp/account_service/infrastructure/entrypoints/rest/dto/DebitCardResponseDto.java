package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto;

import com.nttdata.bootcamp.account_service.domain.model.DebitCardStatus;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Data Transfer Object representing the outbound HTTP REST response payload for debit cards.
 */
@Builder
public record DebitCardResponseDto(

        String id,

        String cardNumber,

        String accountId,

        DebitCardStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
