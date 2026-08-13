package com.nttdata.bootcamp.account_service.application.usecase;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit test suite for {@link DeleteAccountUseCaseImpl}.
 * <p>
 * Technical & Business Rules:
 * - Tests reactive account closure and deletion orchestration.
 * - Enforces zero balance check rule prior to account deletion.
 * - Asserts fail-fast behavior for invalid inputs (null/blank ID, non-existent record).
 * - Employs StepVerifier to assert async completion and error signals non-blockingly.
 * </p>
 *
 * @author NTT DATA Bootcamp Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class DeleteAccountUseCaseImplTest {

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @InjectMocks
    private DeleteAccountUseCaseImpl deleteAccountUseCase;

    @Test
    @DisplayName("Should delete account successfully when account exists and balance is exactly zero")
    void execute_WhenAccountExistsAndBalanceIsZero_ShouldDeleteSuccessfully() {
        // Given
        String id = "ACC-001";
        Account mockAccount = Account.builder()
                .id(id)
                .accountNumber("191-1111111111")
                .type(AccountType.SAVINGS)
                .balance(0.0)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountPersistencePort.findById(id)).thenReturn(Mono.just(mockAccount));
        when(accountPersistencePort.deleteById(id)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = deleteAccountUseCase.execute(id);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(accountPersistencePort).findById(id);
        verify(accountPersistencePort).deleteById(id);
    }

    @Test
    @DisplayName("Should emit error when account holds positive remaining balance")
    void execute_WhenAccountHasPositiveBalance_ShouldEmitIllegalStateException() {
        // Given
        String id = "ACC-002";
        Account mockAccount = Account.builder()
                .id(id)
                .accountNumber("191-2222222222")
                .type(AccountType.CURRENT)
                .balance(150.0)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountPersistencePort.findById(id)).thenReturn(Mono.just(mockAccount));

        // When
        Mono<Void> result = deleteAccountUseCase.execute(id);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalStateException
                        && throwable.getMessage().contains("Balance must be exactly 0.0 before closure"))
                .verify();

        verify(accountPersistencePort).findById(id);
        verify(accountPersistencePort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should emit error when account to delete does not exist")
    void execute_WhenAccountDoesNotExist_ShouldEmitIllegalArgumentException() {
        // Given
        String id = "NON-EXISTENT";
        when(accountPersistencePort.findById(id)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = deleteAccountUseCase.execute(id);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().contains("was not found"))
                .verify();

        verify(accountPersistencePort).findById(id);
        verify(accountPersistencePort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should emit error when account ID is null or blank")
    void execute_WhenIdIsBlankOrNull_ShouldEmitIllegalArgumentException() {
        // When
        Mono<Void> result = deleteAccountUseCase.execute("   ");

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().contains("cannot be null or blank"))
                .verify();

        verify(accountPersistencePort, never()).findById(any());
        verify(accountPersistencePort, never()).deleteById(any());
    }
}