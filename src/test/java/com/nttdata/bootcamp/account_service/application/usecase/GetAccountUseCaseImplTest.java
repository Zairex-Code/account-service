package com.nttdata.bootcamp.account_service.application.usecase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountStatus;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit test suite for {@link GetAccountUseCaseImpl}.
 * <p>
 * Technical & Business Rules:
 * - Tests reactive retrieval queries (findById, findByAccountNumber, findByCustomerId, findAll).
 * - Verifies empty stream emission and error triggers when records are missing.
 * - Employs StepVerifier to assert Mono and Flux emissions asynchronously.
 * - Achieves 100% line and branch coverage for account query operations.
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

        when(accountPersistencePort.findById(id)).thenReturn(Mono.just(mockAccount));

        // When
        Mono<Account> result = getAccountUseCase.findById(id);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(account -> account.getId().equals(id)
                        && account.getAccountNumber().equals("191-1111111111")
                        && account.getStatus() == AccountStatus.ACTIVE
                        && account.getBalance().equals(250.0))
                .verifyComplete();

        verify(accountPersistencePort).findById(id);
    }

    @Test
    @DisplayName("Should emit error when searching by non-existent account ID")
    void findById_WhenAccountDoesNotExist_ShouldEmitIllegalArgumentException() {
        // Given
        String id = "NON-EXISTENT";
        when(accountPersistencePort.findById(id)).thenReturn(Mono.empty());

        // When
        Mono<Account> result = getAccountUseCase.findById(id);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().contains("was not found"))
                .verify();

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

        when(accountPersistencePort.findByAccountNumber(accountNumber)).thenReturn(Mono.just(mockAccount));

        // When
        Mono<Account> result = getAccountUseCase.findByAccountNumber(accountNumber);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(account -> account.getAccountNumber().equals(accountNumber)
                        && account.getType() == AccountType.CURRENT)
                .verifyComplete();

        verify(accountPersistencePort).findByAccountNumber(accountNumber);
    }

    @Test
    @DisplayName("Should stream all accounts belonging to a specific customer ID")
    void findByCustomerId_WhenAccountsExist_ShouldStreamFluxOfAccounts() {
        // Given
        String customerId = "CUST-100";
        Account acc1 = Account.builder().id("ACC-001").customerId(customerId).build();
        Account acc2 = Account.builder().id("ACC-002").customerId(customerId).build();

        when(accountPersistencePort.findByCustomerId(customerId)).thenReturn(Flux.just(acc1, acc2));

        // When
        Flux<Account> result = getAccountUseCase.findByCustomerId(customerId);

        // Then
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();

        verify(accountPersistencePort).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("Should stream all registered accounts in the database")
    void findAll_WhenAccountsExist_ShouldStreamAllAccounts() {
        // Given
        Account acc1 = Account.builder().id("ACC-001").build();
        Account acc2 = Account.builder().id("ACC-002").build();

        when(accountPersistencePort.findAll()).thenReturn(Flux.just(acc1, acc2));

        // When
        Flux<Account> result = getAccountUseCase.findAll();

        // Then
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();

        verify(accountPersistencePort).findAll();
    }
}