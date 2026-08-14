package com.nttdata.bootcamp.account_service.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttdata.bootcamp.account_service.domain.exception.InsufficientBalanceException;
import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountStatus;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.MovementClientPort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test suite for {@link WithdrawAccountUseCaseImpl}.
 * <p>
 * Technical & Business Rules:
 * - Verifies balance debiting and transaction counter advancement.
 * - Verifies error triggers for insufficient balance, invalid inputs, and non-existent accounts.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class WithdrawAccountUseCaseImplTest {

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @Mock
    private MovementClientPort movementClientPort;

    @InjectMocks
    private WithdrawAccountUseCaseImpl withdrawAccountUseCase;

    @Test
    @DisplayName("Should decrease balance when withdrawing from an existing account")
    void withdraw_WhenAccountExists_ShouldDecreaseBalance() {
        String id = "ACC-001";
        Account existingAccount = Account.builder()
                .id(id)
                .accountNumber("191-1111111111")
                .customerId("CUST-001")
                .type(AccountType.SAVINGS)
                .balance(500.0)
                .currentMonthlyTransactions(0)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountPersistencePort.findById(id)).thenReturn(Maybe.just(existingAccount));
        when(accountPersistencePort.save(any(Account.class)))
                .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
        when(movementClientPort.recordMovement(any(String.class), any(String.class),
                any(String.class), any(Double.class))).thenReturn(Completable.complete());

        TestObserver<Account> testObserver = withdrawAccountUseCase.withdraw(id, 100.0).test();

        testObserver.assertValue(account -> account.getBalance().equals(400.0)
                && account.getCurrentMonthlyTransactions() == 1);
        testObserver.assertComplete();

        verify(accountPersistencePort).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit InsufficientBalanceException when withdrawing more than balance")
    void withdraw_WhenInsufficientBalance_ShouldEmitInsufficientBalanceException() {
        String id = "ACC-002";
        Account existingAccount = Account.builder()
                .id(id)
                .accountNumber("191-2222222222")
                .customerId("CUST-001")
                .type(AccountType.SAVINGS)
                .balance(50.0)
                .currentMonthlyTransactions(0)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountPersistencePort.findById(id)).thenReturn(Maybe.just(existingAccount));

        TestObserver<Account> testObserver = withdrawAccountUseCase.withdraw(id, 100.0).test();

        testObserver.assertError(throwable -> throwable instanceof InsufficientBalanceException);

        verify(accountPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should emit error when withdrawing from a non-existent account")
    void withdraw_WhenAccountDoesNotExist_ShouldEmitIllegalArgumentException() {
        String id = "NON-EXISTENT";
        when(accountPersistencePort.findById(id)).thenReturn(Maybe.empty());

        TestObserver<Account> testObserver = withdrawAccountUseCase.withdraw(id, 100.0).test();

        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("was not found"));

        verify(accountPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should emit error when withdrawal amount is not positive")
    void withdraw_WhenAmountIsNotPositive_ShouldEmitIllegalArgumentException() {
        TestObserver<Account> testObserver = withdrawAccountUseCase.withdraw("ACC-001", 0.0).test();

        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("greater than zero"));

        verify(accountPersistencePort, never()).findById(any());
    }

    @Test
    @DisplayName("Should charge commission when withdrawal exceeds the fee-free monthly limit")
    void withdraw_WhenLimitExceeded_ShouldChargeCommission() {
        String id = "ACC-003";
        Account existingAccount = Account.builder()
                .id(id)
                .accountNumber("191-3333333333")
                .customerId("CUST-001")
                .type(AccountType.SAVINGS)
                .balance(100.0)
                .maxMonthlyTransactions(5)
                .currentMonthlyTransactions(5)
                .transactionCommission(2.0)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountPersistencePort.findById(id)).thenReturn(Maybe.just(existingAccount));
        when(accountPersistencePort.save(any(Account.class)))
                .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
        when(movementClientPort.recordMovement(any(String.class), any(String.class),
                any(String.class), any(Double.class))).thenReturn(Completable.complete());

        TestObserver<Account> testObserver = withdrawAccountUseCase.withdraw(id, 10.0).test();

        testObserver.assertValue(account -> account.getBalance().equals(88.0)
                && account.getCurrentMonthlyTransactions() == 6);
        testObserver.assertComplete();

        verify(accountPersistencePort).save(any(Account.class));
    }
}
