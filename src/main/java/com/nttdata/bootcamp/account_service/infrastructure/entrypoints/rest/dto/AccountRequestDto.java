package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto;


import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.Builder;

/**
 * Data Transfer Object representing the incoming HTTP payload for account creation.
 * <p>
 * Technical & Business Rules:
 * - Implemented as a pure Java 17 Record for thread-safe data transfer.
 * - MapStruct 1.5+ maps records natively via their canonical constructor.
 * - Validated at the API edge using Jakarta Bean Validation.
 * </p>
 *
 * @param customerId    Unique database primary key identifier of the owning customer.
 * @param type          Passive account classification (SAVINGS, CURRENT, FIXED_TERM).
 * @param balance       Initial opening monetary balance deposited into the account.
 * @param accountNumber Optional 14-digit bank account number.
 * @param holders       Optional collection of customer IDs acting as joint holders.
 * @param signatories   Optional collection of customer IDs acting as signatories.
 * @author NTT DATA Bootcamp Team
 * @version 1.0
 */
public record AccountRequestDto(

        @NotBlank(message = "Customer ID is required and cannot be blank")
        String customerId,

        @NotNull(message = "Account type is required (SAVINGS, CURRENT, FIXED_TERM)")
        AccountType type,

        @NotNull(message = "Initial balance is required")
        @PositiveOrZero(message = "Initial balance cannot be negative")
        Double balance,

        String accountNumber,

        List<String> holders,

        List<String> signatories
) {
}