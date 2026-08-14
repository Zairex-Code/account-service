package com.nttdata.bootcamp.account_service.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
 * Unit test suite for {@link TransferAccountUseCaseImpl}.
 * <p>
 * Technical & Business Rules:
 * - Verifies balance movement between source and destination accounts.
 * - Verifies error triggers for insufficient balance, invalid inputs, and missing accounts.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class TransferAccountUseCaseImplTest {

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @Mock
    private MovementClientPort movementClientPort;

    @InjectMocks
    private TransferAccountUseCaseImpl transferAccountUseCase;

    private Account savingsAccount(String id, double balance) {
        return Account.builder()
                .id(id)
                .accountNumber("191-0000000000")
                .customerId("CUST-001")
                .type(AccountType.SAVINGS)
                .balance(balance)
                .currentMonthlyTransactions(0)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should debit source and credit destination when both accounts exist")
    void transfer_WhenBothAccountsExist_ShouldMoveBalance() {
        Account source = savingsAccount("ACC-SRC", 500.0);
        Account destination = savingsAccount("ACC-DST", 100.0);

        when(accountPersistencePort.findById("ACC-SRC")).thenReturn(Maybe.just(source));
        when(accountPersistencePort.findById("ACC-DST")).thenReturn(Maybe.just(destination));
        when(accountPersistencePort.save(any(Account.class)))
                .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
        when(movementClientPort.recordMovement(anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(Completable.complete());

        TestObserver<Account> testObserver =
                transferAccountUseCase.transfer("ACC-SRC", "ACC-DST", 100.0).test();

        testObserver.assertValue(account -> account.getBalance().equals(400.0)
                && account.getCurrentMonthlyTransactions() == 1);
        testObserver.assertComplete();

        verify(accountPersistencePort, times(2)).save(any(Account.class));
        verify(movementClientPort, times(2))
                .recordMovement(anyString(), anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("Should emit InsufficientBalanceException when source balance is below amount")
    void transfer_WhenInsufficientBalance_ShouldEmitInsufficientBalanceException() {
        Account source = savingsAccount("ACC-SRC", 50.0);
        Account destination = savingsAccount("ACC-DST", 100.0);

        when(accountPersistencePort.findById("ACC-SRC")).thenReturn(Maybe.just(source));
        when(accountPersistencePort.findById("ACC-DST")).thenReturn(Maybe.just(destination));

        TestObserver<Account> testObserver =
                transferAccountUseCase.transfer("ACC-SRC", "ACC-DST", 100.0).test();

        testObserver.assertError(throwable -> throwable instanceof InsufficientBalanceException);

        verify(accountPersistencePort, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit error when source account does not exist")
    void transfer_WhenSourceNotFound_ShouldEmitIllegalArgumentException() {
        when(accountPersistencePort.findById("ACC-SRC")).thenReturn(Maybe.empty());

        TestObserver<Account> testObserver =
                transferAccountUseCase.transfer("ACC-SRC", "ACC-DST", 100.0).test();

        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("source account"));

        verify(accountPersistencePort, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit error when destination account does not exist")
    void transfer_WhenDestinationNotFound_ShouldEmitIllegalArgumentException() {
        Account source = savingsAccount("ACC-SRC", 500.0);

        when(accountPersistencePort.findById("ACC-SRC")).thenReturn(Maybe.just(source));
        when(accountPersistencePort.findById("ACC-DST")).thenReturn(Maybe.empty());

        TestObserver<Account> testObserver =
                transferAccountUseCase.transfer("ACC-SRC", "ACC-DST", 100.0).test();

        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("destination account"));

        verify(accountPersistencePort, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit error when source and destination are the same account")
    void transfer_WhenSameAccount_ShouldEmitIllegalArgumentException() {
        TestObserver<Account> testObserver =
                transferAccountUseCase.transfer("ACC-SRC", "ACC-SRC", 100.0).test();

        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("different"));

        verify(accountPersistencePort, never()).findById(anyString());
    }

    @Test
    @DisplayName("Should emit error when transfer amount is not positive")
    void transfer_WhenAmountNotPositive_ShouldEmitIllegalArgumentException() {
        TestObserver<Account> testObserver =
                transferAccountUseCase.transfer("ACC-SRC", "ACC-DST", 0.0).test();

        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("greater than zero"));

        verify(accountPersistencePort, never()).findById(anyString());
    }
}
