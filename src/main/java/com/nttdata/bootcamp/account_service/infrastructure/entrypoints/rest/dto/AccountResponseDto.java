package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto;


import com.nttdata.bootcamp.account_service.domain.model.AccountStatus;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing the outbound HTTP REST response payload for bank account operations.
 * <p>
 * Technical & Business Rules:
 * - Encapsulates account details returned to external clients and API Consumers.
 * - Hides internal infrastructure details while providing complete financial domain information.
 * - Ensures immutability and consistent serialisation format across all REST endpoints.
 * </p>

 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDto {

    /**
     * Unique database primary key identifier of the bank account.
     */
    private String id;

    /**
     * Unique 14-digit public bank account number (e.g., "191-0048291042").
     */
    private String accountNumber;

    /**
     * Unique identifier of the customer owning this bank account.
     */
    private String customerId;

    /**
     * Passive financial product classification (SAVINGS, CURRENT, FIXED_TERM).
     */
    private AccountType type;

    /**
     * Current operational status of the account (ACTIVE, BLOCKED, INACTIVE, CLOSED).
     */
    private AccountStatus status;

    /**
     * Current available monetary balance in the account.
     */
    private Double balance;

    /**
     * Monthly maintenance fee applied to current accounts.
     */
    private Double maintenanceFee;

    /**
     * Maximum number of fee-free monthly transactions allowed.
     */
    private Integer maxMonthlyTransactions;

    /**
     * Total number of transactions performed within the current monthly cycle.
     */
    private Integer currentMonthlyTransactions;

    /**
     * Specific calendar day of the month permitted for fixed-term transactions.
     */
    private Integer allowedTransactionDay;

    /**
     * Collection of customer IDs acting as joint account holders.
     */
    private List<String> holders;

    /**
     * Collection of customer IDs acting as legal authorized signatories.
     */
    private List<String> signatories;

    /**
     * Audit timestamp indicating when the account was opened.
     */
    private LocalDateTime createdAt;

    /**
     * Audit timestamp indicating the latest modification date and time.
     */
    private LocalDateTime updatedAt;
}