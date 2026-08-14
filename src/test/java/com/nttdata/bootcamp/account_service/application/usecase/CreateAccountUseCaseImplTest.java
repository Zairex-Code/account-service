package com.nttdata.bootcamp.account_service.application.usecase;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountStatus;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import com.nttdata.bootcamp.account_service.domain.model.CustomerInfo;
import com.nttdata.bootcamp.account_service.domain.model.CustomerProfile;
import com.nttdata.bootcamp.account_service.domain.model.CustomerType;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.CustomerClientPort;
import io.reactivex.rxjava3.core.Flowable;
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
 * Unit test suite for {@link CreateAccountUseCaseImpl}.
 * <p>
 * Technical & Business Rules:
 * - Employs TestObserver to test reactive stream execution and assertions without blocking.
 * - Mocks external ports (AccountPersistencePort, CustomerClientPort) using Mockito.
 * - Verifies fail-fast business rules (null payload, negative balance, non-existent customer, limits).
 * </p>

 */
@ExtendWith(MockitoExtension.class)
class CreateAccountUseCaseImplTest {

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @Mock
    private CustomerClientPort customerClientPort;

    @InjectMocks
    private CreateAccountUseCaseImpl createAccountUseCase;

    @Test
    @DisplayName("Should create account successfully when customer exists and payload is valid")
    void execute_WhenCustomerExistsAndValidPayload_ShouldReturnCreatedAccount() {
        // Given
        String customerId = "CUST-001";
        Account inputAccount = Account.builder()
                .customerId(customerId)
                .type(AccountType.SAVINGS)
                .balance(500.0)
                .build();

        Account savedAccount = inputAccount.toBuilder()
                .id("ACC-999")
                .accountNumber("191-0001234567")
                .status(AccountStatus.ACTIVE)
                .build();

        when(customerClientPort.getById(customerId)).thenReturn(Maybe.just(
                new CustomerInfo(customerId, CustomerType.PERSONAL, CustomerProfile.STANDARD)));
        when(accountPersistencePort.findByCustomerIdAndType(customerId, AccountType.SAVINGS))
                .thenReturn(Flowable.empty());
        when(accountPersistencePort.save(any(Account.class))).thenReturn(Single.just(savedAccount));

        // When
        TestObserver<Account> testObserver = createAccountUseCase.execute(inputAccount).test();

        // Then
        testObserver.assertValue(account -> account.getId().equals("ACC-999")
                && account.getAccountNumber().startsWith("191-")
                && account.getStatus() == AccountStatus.ACTIVE
                && account.getBalance().equals(500.0));
        testObserver.assertComplete();

        verify(customerClientPort).getById(customerId);
        verify(accountPersistencePort).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit error when customer does not exist in customer-service")
    void execute_WhenCustomerDoesNotExist_ShouldEmitIllegalArgumentException() {
        // Given
        String customerId = "NON-EXISTENT";
        Account inputAccount = Account.builder()
                .customerId(customerId)
                .type(AccountType.SAVINGS)
                .balance(100.0)
                .build();

        when(customerClientPort.getById(customerId)).thenReturn(Maybe.empty());

        // When
        TestObserver<Account> testObserver = createAccountUseCase.execute(inputAccount).test();

        // Then
        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("does not exist"));

        verify(accountPersistencePort, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit error when personal customer already holds a savings account")
    void execute_WhenPersonalAlreadyHoldsSavings_ShouldEmitIllegalStateException() {
        // Given
        String customerId = "CUST-001";
        Account inputAccount = Account.builder()
                .customerId(customerId)
                .type(AccountType.SAVINGS)
                .balance(100.0)
                .build();

        when(customerClientPort.getById(customerId)).thenReturn(Maybe.just(
                new CustomerInfo(customerId, CustomerType.PERSONAL, CustomerProfile.STANDARD)));
        when(accountPersistencePort.findByCustomerIdAndType(customerId, AccountType.SAVINGS))
                .thenReturn(Flowable.just(Account.builder().id("ACC-001").build()));

        // When
        TestObserver<Account> testObserver = createAccountUseCase.execute(inputAccount).test();

        // Then
        testObserver.assertError(throwable -> throwable instanceof IllegalStateException
                && throwable.getMessage().contains("Only one is allowed"));

        verify(accountPersistencePort, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit error when business customer requests a savings account")
    void execute_WhenBusinessRequestsSavings_ShouldEmitIllegalStateException() {
        // Given
        String customerId = "CUST-BIZ-001";
        Account inputAccount = Account.builder()
                .customerId(customerId)
                .type(AccountType.SAVINGS)
                .balance(100.0)
                .build();

        when(customerClientPort.getById(customerId)).thenReturn(Maybe.just(
                new CustomerInfo(customerId, CustomerType.BUSINESS, CustomerProfile.STANDARD)));

        // When
        TestObserver<Account> testObserver = createAccountUseCase.execute(inputAccount).test();

        // Then
        testObserver.assertError(throwable -> throwable instanceof IllegalStateException
                && throwable.getMessage().contains("Business customers cannot acquire"));

        verify(accountPersistencePort, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit error when account balance is negative")
    void execute_WhenBalanceIsNegative_ShouldEmitIllegalArgumentException() {
        // Given
        Account inputAccount = Account.builder()
                .customerId("CUST-001")
                .type(AccountType.SAVINGS)
                .balance(-50.0)
                .build();

        // When
        TestObserver<Account> testObserver = createAccountUseCase.execute(inputAccount).test();

        // Then
        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("Initial balance cannot be null or negative"));

        verify(customerClientPort, never()).getById(any());
        verify(accountPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should emit error when account payload is null")
    void execute_WhenNullPayload_ShouldEmitIllegalArgumentException() {
        // When
        TestObserver<Account> testObserver = createAccountUseCase.execute(null).test();

        // Then
        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().equals("Account payload cannot be null"));

        verify(customerClientPort, never()).getById(any());
        verify(accountPersistencePort, never()).save(any());
    }
}
