package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto;


import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing the incoming HTTP REST payload for account creation.
 * <p>
 * Technical & Business Rules:
 * - Enforces edge validation using Jakarta Bean Validation annotations (@NotBlank, @NotNull, @PositiveOrZero).
 * - Decouples external API contracts from pure domain entities (Account.java).
 * - Supports optional customized account numbers, additional holders, and authorized signatories.
 * </p>

 * @author NTT DATA Bootcamp Team
 * @version 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestDto {

    /**
     * Unique database primary key identifier of the owning customer.
     */
    @NotBlank(message = "Customer ID is required and cannot be blank")
    private String customerId;

    /**
     * Passive financial account product type (SAVINGS, CURRENT, FIXED_TERM).
     */
    @NotNull(message = "Account type is required (SAVINGS, CURRENT, FIXED_TERM)")
    private AccountType type;

    /**
     * Opening initial monetary balance deposited into the account.
     */
    @NotNull(message = "Initial balance is required")
    @PositiveOrZero(message = "Initial balance cannot be negative")
    private Double balance;

    /**
     * Optional 14-digit bank account number. If omitted, the core engine auto-generates one.
     */
    private String accountNumber;

    /**
     * Optional collection of customer IDs acting as joint account holders (e.g., corporate accounts).
     */
    private List<String> holders;

    /**
     * Optional collection of customer IDs acting as legal authorized signatories (e.g., corporate accounts).
     */
    private List<String> signatories;
}