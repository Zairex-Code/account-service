package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto;


import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.Builder;

/**
 * Data Transfer Object representing the incoming HTTP REST payload for account creation.
 * <p>
 * Technical & Business Rules :
 * - Implemented as an immutable Java 17 Record for thread-safe data transfer across layers.
 * - Enforces edge validation using Jakarta Bean Validation annotations directly on record components.
 * - Supports Lombok @Builder for fluent instance creation in tests and mappers.
 * - Decouples external API contracts from internal domain models.
 * </p>

 * @author NTT DATA Bootcamp Team
 * @version 1.0
 */
@Builder
public record AccountRequestDto(

        /**
         * Unique database primary key identifier of the owning customer.
         */
        @NotBlank(message = "Customer ID is required and cannot be blank")
        String customerId,

        /**
         * Passive financial account product type (SAVINGS, CURRENT, FIXED_TERM).
         */
        @NotNull(message = "Account type is required (SAVINGS, CURRENT, FIXED_TERM)")
        AccountType type,

        /**
         * Opening initial monetary balance deposited into the account.
         */
        @NotNull(message = "Initial balance is required")
        @PositiveOrZero(message = "Initial balance cannot be negative")
        Double balance,

        /**
         * Optional 14-digit bank account number. If omitted, the core engine auto-generates one.
         */
        String accountNumber,

        /**
         * Optional collection of customer IDs acting as joint account holders (e.g., corporate accounts).
         */
        List<String> holders,

        /**
         * Optional collection of customer IDs acting as legal authorized signatories (e.g., corporate accounts).
         */
        List<String> signatories
) {
}