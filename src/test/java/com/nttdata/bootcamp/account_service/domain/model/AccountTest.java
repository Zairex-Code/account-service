package com.nttdata.bootcamp.account_service.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nttdata.bootcamp.account_service.domain.exception.InsufficientBalanceException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for the {@link Account} rich domain entity.
 */
class AccountTest {

    private Account account(AccountStatus status, double balance) {
        return Account.builder()
                .id("ACC-001")
                .accountNumber("191-1111111111")
                .customerId("CUST-001")
                .type(AccountType.SAVINGS)
                .status(status)
                .balance(balance)
                .maxMonthlyTransactions(5)
                .currentMonthlyTransactions(0)
                .transactionCommission(2.0)
                .build();
    }

    @Test
    @DisplayName("Should increase balance and transaction count on deposit")
    void deposit_ShouldIncreaseBalance() {
        Account updated = account(AccountStatus.ACTIVE, 100.0).deposit(50.0);

        assertEquals(150.0, updated.getBalance());
        assertEquals(1, updated.getCurrentMonthlyTransactions());
    }

    @Test
    @DisplayName("Should reject non-positive deposit amount")
    void deposit_WhenAmountNotPositive_ShouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> account(AccountStatus.ACTIVE, 100.0).deposit(0.0));
    }

    @Test
    @DisplayName("Should reject deposit on non-active account")
    void deposit_WhenInactive_ShouldThrow() {
        assertThrows(IllegalStateException.class,
                () -> account(AccountStatus.BLOCKED, 100.0).deposit(10.0));
    }

    @Test
    @DisplayName("Should decrease balance on withdrawal")
    void withdraw_ShouldDecreaseBalance() {
        Account updated = account(AccountStatus.ACTIVE, 100.0).withdraw(30.0);

        assertEquals(70.0, updated.getBalance());
        assertEquals(1, updated.getCurrentMonthlyTransactions());
    }

    @Test
    @DisplayName("Should reject non-positive withdrawal amount")
    void withdraw_WhenAmountNotPositive_ShouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> account(AccountStatus.ACTIVE, 100.0).withdraw(0.0));
    }

    @Test
    @DisplayName("Should reject withdrawal on non-active account")
    void withdraw_WhenInactive_ShouldThrow() {
        assertThrows(IllegalStateException.class,
                () -> account(AccountStatus.BLOCKED, 100.0).withdraw(10.0));
    }

    @Test
    @DisplayName("Should emit InsufficientBalanceException when balance is below amount")
    void withdraw_WhenInsufficientBalance_ShouldThrow() {
        assertThrows(InsufficientBalanceException.class,
                () -> account(AccountStatus.ACTIVE, 50.0).withdraw(100.0));
    }

    @Test
    @DisplayName("Should charge commission when fee-free monthly limit is exceeded")
    void withdraw_WhenLimitExceeded_ShouldChargeCommission() {
        Account atLimit = account(AccountStatus.ACTIVE, 100.0).toBuilder()
                .currentMonthlyTransactions(5)
                .build();

        Account updated = atLimit.withdraw(10.0);

        assertEquals(88.0, updated.getBalance());
        assertEquals(6, updated.getCurrentMonthlyTransactions());
    }

    @Test
    @DisplayName("Should not charge commission when within fee-free limit")
    void withdraw_WhenWithinLimit_ShouldNotChargeCommission() {
        Account updated = account(AccountStatus.ACTIVE, 100.0).withdraw(10.0);
        assertEquals(90.0, updated.getBalance());
    }

    @Test
    @DisplayName("hasReachedTransactionLimit should be false when max is null")
    void hasReachedTransactionLimit_WhenMaxNull_ShouldBeFalse() {
        Account noLimit = account(AccountStatus.ACTIVE, 100.0).toBuilder()
                .maxMonthlyTransactions(null)
                .build();
        assertFalse(noLimit.hasReachedTransactionLimit());
    }

    @Test
    @DisplayName("hasReachedTransactionLimit should be true when current equals max")
    void hasReachedTransactionLimit_WhenCurrentEqualsMax_ShouldBeTrue() {
        Account atLimit = account(AccountStatus.ACTIVE, 100.0).toBuilder()
                .currentMonthlyTransactions(5)
                .build();
        assertTrue(atLimit.hasReachedTransactionLimit());
    }

    @Test
    @DisplayName("hasReachedTransactionLimit should be false when current below max")
    void hasReachedTransactionLimit_WhenBelowMax_ShouldBeFalse() {
        assertFalse(account(AccountStatus.ACTIVE, 100.0).hasReachedTransactionLimit());
    }

    @Test
    @DisplayName("Should transition status to BLOCKED")
    void block_ShouldSetBlockedStatus() {
        assertEquals(AccountStatus.BLOCKED, account(AccountStatus.ACTIVE, 100.0).block().getStatus());
    }

    @Test
    @DisplayName("Should transition status to ACTIVE")
    void activate_ShouldSetActiveStatus() {
        assertEquals(AccountStatus.ACTIVE, account(AccountStatus.BLOCKED, 100.0).activate().getStatus());
    }

    @Test
    @DisplayName("getHolders should return empty list when null")
    void getHolders_WhenNull_ShouldReturnEmpty() {
        assertTrue(account(AccountStatus.ACTIVE, 100.0).getHolders().isEmpty());
    }

    @Test
    @DisplayName("getHolders should return populated list when provided")
    void getHolders_WhenPopulated_ShouldReturnList() {
        Account withHolders = account(AccountStatus.ACTIVE, 100.0).toBuilder()
                .holders(List.of("CUST-A", "CUST-B"))
                .build();
        assertEquals(2, withHolders.getHolders().size());
    }

    @Test
    @DisplayName("getSignatories should return empty list when null")
    void getSignatories_WhenNull_ShouldReturnEmpty() {
        assertTrue(account(AccountStatus.ACTIVE, 100.0).getSignatories().isEmpty());
    }

    @Test
    @DisplayName("getSignatories should return populated list when provided")
    void getSignatories_WhenPopulated_ShouldReturnList() {
        Account withSignatories = account(AccountStatus.ACTIVE, 100.0).toBuilder()
                .signatories(List.of("CUST-X"))
                .build();
        assertEquals(1, withSignatories.getSignatories().size());
    }
}
