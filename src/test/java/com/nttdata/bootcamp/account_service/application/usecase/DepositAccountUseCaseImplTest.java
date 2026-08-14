package com.nttdata.bootcamp.account_service.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
 * Unit test suite for {@link DepositAccountUseCaseImpl}.
 * <p>
 * Technical & Business Rules:
 * - Verifies balance crediting and transaction counter advancement.
 * - Verifies error triggers for invalid inputs and non-existent accounts.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class DepositAccountUseCaseImplTest {

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @Mock
    private MovementClientPort movementClientPort;

    @InjectMocks
    private DepositAccountUseCaseImpl depositAccountUseCase;

    @Test
    @DisplayName("Should increase balance when depositing into an existing account")
    void deposit_WhenAccountExists_ShouldIncreaseBalance() {
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

        TestObserver<Account> testObserver = depositAccountUseCase.deposit(id, 100.0).test();

        testObserver.assertValue(account -> account.getBalance().equals(600.0)
                && account.getCurrentMonthlyTransactions() == 1);
        testObserver.assertComplete();

        verify(accountPersistencePort).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit error when depositing into a non-existent account")
    void deposit_WhenAccountDoesNotExist_ShouldEmitIllegalArgumentException() {
        String id = "NON-EXISTENT";
        when(accountPersistencePort.findById(id)).thenReturn(Maybe.empty());

        TestObserver<Account> testObserver = depositAccountUseCase.deposit(id, 100.0).test();

        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("was not found"));

        verify(accountPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should emit error when deposit amount is not positive")
    void deposit_WhenAmountIsNotPositive_ShouldEmitIllegalArgumentException() {
        TestObserver<Account> testObserver = depositAccountUseCase.deposit("ACC-001", 0.0).test();

        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("greater than zero"));

        verify(accountPersistencePort, never()).findById(any());
    }
}
