package com.nttdata.bootcamp.account_service.domain.model;


import com.nttdata.bootcamp.account_service.domain.exception.InsufficientBalanceException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


/**
 * Pure Java Domain representing a Bank Account within the core engine.
 *
 * Technical & Business Rules:
 * - Pure POJO decoupled from Spring Framework, MongoDB, or transport layers
 * - Immutable state management using Lombok Builder with 'toBuilder = true'
 * - Encapsulates rich financial behavior: deposit, withdrawal, transaction limits, and status updates
 * - Supports personal and business accounts, including multiple holders and signatories
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {


    private final String id;
    private final String accountNumber;
    private final String customerId;
    private final AccountType type;
    private final AccountStatus status;
    private final Double balance;
    private final Double maintenanceFee;
    private final Integer maxMonthlyTransactions;
    private final Integer currentMonthlyTransactions;
    private final Integer allowedTransactionDay;
    private final Double transactionCommission;
    private final List<String> holders;
    private final List<String> signatories;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;


    /**
     * Credits a positie monetary amount to the account balance
     *
     * @param amount Positive amount to deposit
     * @return A new immutable Account instance with updated balance and transaction count.
     */
    public Account deposit(Double amount){
        if (amount == null || amount <= 0){
            throw new IllegalArgumentException("Deposit amount must be strictly grater than zero");
        }
        if (this.status != AccountStatus.ACTIVE){
            throw new IllegalStateException("Cannot process deposit on account in status: "+ this.status);
        }

        return this.toBuilder()
                .balance(this.balance + amount)
                .currentMonthlyTransactions(this.currentMonthlyTransactions + 1)
                .updatedAt(LocalDateTime.now())
                .build();
    }


    /**
     * Debits a positive monetary amount from the account balance
     * <p>
     * Business Rule: when the fee-free monthly transaction limit is exceeded, a
     * transaction commission is automatically charged together with the withdrawal.
     * </p>
     *
     * @param amount Positive amount to withdraw
     * @return A new immutable Account instance with updated balance and transaction count
     */
    public Account withdraw(Double amount){
        if (amount == null || amount <= 0){
            throw new IllegalArgumentException("Withdrawal amount must be strictly greater than zero");
        }
        if (this.status != AccountStatus.ACTIVE){
            throw new IllegalStateException("Cannot process withdrawal on account in status: " + this.status);
        }

        double commission = transactionCommission();
        double totalDebit = amount + commission;

        if (this.balance < totalDebit){
            throw new InsufficientBalanceException(this.accountNumber, this.balance, totalDebit);
        }
        return this.toBuilder()
                .balance(this.balance - totalDebit)
                .currentMonthlyTransactions(this.currentMonthlyTransactions + 1)
                .updatedAt(LocalDateTime.now())
                .build();
    }


    /**
     * Calculates the automatic transaction commission charged when the fee-free
     * monthly transaction limit has been reached.
     *
     * @return The commission amount to charge, or 0.0 when no commission applies.
     */
    private double transactionCommission(){
        if (this.transactionCommission == null || this.transactionCommission <= 0){
            return 0.0;
        }
        if (this.maxMonthlyTransactions == null){
            return 0.0;
        }
        int current = this.currentMonthlyTransactions == null ? 0 : this.currentMonthlyTransactions;
        if (current >= this.maxMonthlyTransactions){
            return this.transactionCommission;
        }
        return 0.0;
    }


    /**
     * Evaluate whether the account has reached its fee-free monthly transaction limit
     *
     * @return true if maxMonthlyTransactions is defined and current count equals or exceeds it
     */
    public boolean hasReachedTransactionLimit(){
        return this.maxMonthlyTransactions != null && this.currentMonthlyTransactions >= this.maxMonthlyTransactions;
    }


    /**
     * Transitions account operational status to BLOCKED
     *
     * @return A new immutable Account instance in BLOCKED state
     */
    public Account block(){
        return this.toBuilder()
                .status(AccountStatus.BLOCKED)
                .updatedAt(LocalDateTime.now())
                .build();
    }


    /**
     * Transitions account operational status to ACTIVE
     *
     * @return A new immutable Account instance in ACTIVE status
     */
    public Account activate(){
        return this.toBuilder()
                .status(AccountStatus.ACTIVE)
                .updatedAt(LocalDateTime.now())
                .build();
    }


    /**
     * Returns an unmodifiable list of additional account holders
     *
     * @return Unmodifiable List of customer IDs acting as additional holders
     */
    public List<String> getHolders(){
        return holders != null ? Collections.unmodifiableList(holders) : Collections.emptyList();
    }


    /**
     * Returns an unmodifiable list authorized signatories
     * @return Unmodiable List of customer IDs acting as authorized signatories
     */
    public List<String> getSignatories(){
        return signatories != null ? Collections.unmodifiableList(signatories) : Collections.emptyList();
    }
}
