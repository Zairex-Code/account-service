package com.nttdata.bootcamp.account_service.application.usecase;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountStatus;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.CustomerClientPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit test suite for {@link CreateAccountUseCaseImpl}.
 * <p>
 * Technical & Business Rules:
 * - Employs StepVerifier to test reactive stream execution and assertions without blocking.
 * - Mocks external ports (AccountPersistencePort, CustomerClientPort) using Mockito.
 * - Verifies fail-fast business rules (null payload, negative balance, non-existent customer).
 * - Ensures 100% line and branch coverage for account creation logic.
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

        when(customerClientPort.existsById(customerId)).thenReturn(Mono.just(true));
        when(accountPersistencePort.save(any(Account.class))).thenReturn(Mono.just(savedAccount));

        // When
        Mono<Account> result = createAccountUseCase.execute(inputAccount);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(account -> account.getId().equals("ACC-999")
                        && account.getAccountNumber().startsWith("191-")
                        && account.getStatus() == AccountStatus.ACTIVE
                        && account.getBalance().equals(500.0))
                .verifyComplete();

        verify(customerClientPort).existsById(customerId);
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

        when(customerClientPort.existsById(customerId)).thenReturn(Mono.just(false));

        // When
        Mono<Account> result = createAccountUseCase.execute(inputAccount);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().contains("does not exist"))
                .verify();

        verify(customerClientPort).existsById(customerId);
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
        Mono<Account> result = createAccountUseCase.execute(inputAccount);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().contains("Initial balance cannot be null or negative"))
                .verify();

        verify(customerClientPort, never()).existsById(any());
        verify(accountPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should emit error when account payload is null")
    void execute_WhenNullPayload_ShouldEmitIllegalArgumentException() {
        // When
        Mono<Account> result = createAccountUseCase.execute(null);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().equals("Account payload cannot be null"))
                .verify();

        verify(customerClientPort, never()).existsById(any());
        verify(accountPersistencePort, never()).save(any());
    }
}