package com.nttdata.bootcamp.account_service.application.usecase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountStatus;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test suite for {@link GetAccountUseCaseImpl}.
 * <p>
 * Technical & Business Rules:
 * - Tests reactive retrieval queries (findById, findByAccountNumber, findByCustomerId, findAll).
 * - Verifies empty stream emission and error triggers when records are missing.
 * - Employs TestObserver/TestSubscriber to assert Single, Maybe and Flowable emissions asynchronously.
 * </p>

 * @author NTT DATA Bootcamp Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class GetAccountUseCaseImplTest {

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @InjectMocks
    private GetAccountUseCaseImpl getAccountUseCase;

    @Test
    @DisplayName("Should return account when searching by valid existing primary ID")
    void findById_WhenAccountExists_ShouldReturnAccount() {
        // Given
        String id = "ACC-001";
        Account mockAccount = Account.builder()
                .id(id)
                .accountNumber("191-1111111111")
                .type(AccountType.SAVINGS)
                .balance(250.0)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountPersistencePort.findById(id)).thenReturn(Maybe.just(mockAccount));

        // When
        TestObserver<Account> testObserver = getAccountUseCase.findById(id).test();

        // Then
        testObserver.assertValue(account -> account.getId().equals(id)
                && account.getAccountNumber().equals("191-1111111111")
                && account.getStatus() == AccountStatus.ACTIVE
                && account.getBalance().equals(250.0));
        testObserver.assertComplete();

        verify(accountPersistencePort).findById(id);
    }

    @Test
    @DisplayName("Should emit error when searching by non-existent account ID")
    void findById_WhenAccountDoesNotExist_ShouldEmitIllegalArgumentException() {
        // Given
        String id = "NON-EXISTENT";
        when(accountPersistencePort.findById(id)).thenReturn(Maybe.empty());

        // When
        TestObserver<Account> testObserver = getAccountUseCase.findById(id).test();

        // Then
        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("was not found"));

        verify(accountPersistencePort).findById(id);
    }

    @Test
    @DisplayName("Should return account when searching by valid public account number")
    void findByAccountNumber_WhenExists_ShouldReturnAccount() {
        // Given
        String accountNumber = "191-2222222222";
        Account mockAccount = Account.builder()
                .id("ACC-002")
                .accountNumber(accountNumber)
                .type(AccountType.CURRENT)
                .balance(1000.0)
                .build();

        when(accountPersistencePort.findByAccountNumber(accountNumber)).thenReturn(Maybe.just(mockAccount));

        // When
        TestObserver<Account> testObserver = getAccountUseCase.findByAccountNumber(accountNumber).test();

        // Then
        testObserver.assertValue(account -> account.getAccountNumber().equals(accountNumber)
                && account.getType() == AccountType.CURRENT);
        testObserver.assertComplete();

        verify(accountPersistencePort).findByAccountNumber(accountNumber);
    }

    @Test
    @DisplayName("Should stream all accounts belonging to a specific customer ID")
    void findByCustomerId_WhenAccountsExist_ShouldStreamFlowableOfAccounts() {
        // Given
        String customerId = "CUST-100";
        Account acc1 = Account.builder().id("ACC-001").customerId(customerId).build();
        Account acc2 = Account.builder().id("ACC-002").customerId(customerId).build();

        when(accountPersistencePort.findByCustomerId(customerId)).thenReturn(Flowable.just(acc1, acc2));

        // When
        TestSubscriber<Account> testSubscriber = getAccountUseCase.findByCustomerId(customerId).test();

        // Then
        testSubscriber.assertValueCount(2);
        testSubscriber.assertComplete();

        verify(accountPersistencePort).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("Should stream all registered accounts in the database")
    void findAll_WhenAccountsExist_ShouldStreamAllAccounts() {
        // Given
        Account acc1 = Account.builder().id("ACC-001").build();
        Account acc2 = Account.builder().id("ACC-002").build();

        when(accountPersistencePort.findAll()).thenReturn(Flowable.just(acc1, acc2));

        // When
        TestSubscriber<Account> testSubscriber = getAccountUseCase.findAll().test();

        // Then
        testSubscriber.assertValueCount(2);
        testSubscriber.assertComplete();

        verify(accountPersistencePort).findAll();
    }

    @Test
    @DisplayName("Should stream a consolidated report of accounts within a date range")
    void findByCustomerIdAndDateRange_WhenAccountsExist_ShouldStreamAccounts() {
        // Given
        String customerId = "CUST-100";
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59, 59);

        Account acc1 = Account.builder().id("ACC-001").customerId(customerId).build();
        Account acc2 = Account.builder().id("ACC-002").customerId(customerId).build();

        when(accountPersistencePort.findByCustomerIdAndDateRange(customerId, start, end))
                .thenReturn(Flowable.just(acc1, acc2));

        // When
        TestSubscriber<Account> testSubscriber =
                getAccountUseCase.findByCustomerIdAndDateRange(customerId, start, end).test();

        // Then
        testSubscriber.assertValueCount(2);
        testSubscriber.assertComplete();

        verify(accountPersistencePort).findByCustomerIdAndDateRange(customerId, start, end);
    }
}
