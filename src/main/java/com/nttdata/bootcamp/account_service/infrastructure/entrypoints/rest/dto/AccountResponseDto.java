package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto;


import com.nttdata.bootcamp.account_service.domain.model.AccountStatus;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * Data Transfer Object representing the outbound HTTP REST response payload for bank account operations.
 * <p>
 * Technical & Business Rules :
 * - Implemented as an immutable Java 17 Record for high-performance serialisation and thread safety.
 * - Encapsulates account details returned to external API clients without exposing database entities.
 * - Supports Lombok @Builder on records for fluent instantiation inside mappers.
 * </p>
 *
 * @author NTT DATA Bootcamp Team
 * @version 1.0
 */
@Builder
public record AccountResponseDto(

        /**
         * Unique database primary key identifier of the bank account.
         */
        String id,

        /**
         * Unique 14-digit public bank account number (e.g., "191-0048291042").
         */
        String accountNumber,

        /**
         * Unique identifier of the customer owning this bank account.
         */
        String customerId,

        /**
         * Passive financial product classification (SAVINGS, CURRENT, FIXED_TERM).
         */
        AccountType type,

        /**
         * Current operational status of the account (ACTIVE, BLOCKED, INACTIVE, CLOSED).
         */
        AccountStatus status,

        /**
         * Current available monetary balance in the account.
         */
        Double balance,

        /**
         * Monthly maintenance fee applied to current accounts.
         */
        Double maintenanceFee,

        /**
         * Maximum number of fee-free monthly transactions allowed.
         */
        Integer maxMonthlyTransactions,

        /**
         * Total number of transactions performed within the current monthly cycle.
         */
        Integer currentMonthlyTransactions,

        /**
         * Specific calendar day of the month permitted for fixed-term transactions.
         */
        Integer allowedTransactionDay,

        /**
         * Collection of customer IDs acting as joint account holders.
         */
        List<String> holders,

        /**
         * Collection of customer IDs acting as legal authorized signatories.
         */
        List<String> signatories,

        /**
         * Audit timestamp indicating when the account was opened.
         */
        LocalDateTime createdAt,

        /**
         * Audit timestamp indicating the latest modification date and time.
         */
        LocalDateTime updatedAt
) {
}