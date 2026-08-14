package com.nttdata.bootcamp.account_service.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountStatus;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test suite for {@link UpdateAccountUseCaseImpl}.
 * <p>
 * Technical & Business Rules:
 * - Tests reactive account update operations and domain merging rules.
 * - Validates fail-fast exceptions on invalid inputs (null ID, null payload, missing record).
 * - Employs TestObserver to assert Single emissions asynchronously without blocking Netty threads.
 * </p>

 * @author NTT DATA Bootcamp Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class UpdateAccountUseCaseImplTest {

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @InjectMocks
    private UpdateAccountUseCaseImpl updateAccountUseCase;

    @Test
    @DisplayName("Should update account successfully when account exists and payload is valid")
    void execute_WhenAccountExistsAndValidPayload_ShouldReturnUpdatedAccount() {
        // Given
        String id = "ACC-001";
        Account existingAccount = Account.builder()
                .id(id)
                .accountNumber("191-1111111111")
                .customerId("CUST-001")
                .type(AccountType.SAVINGS)
                .balance(200.0)
                .status(AccountStatus.ACTIVE)
                .build();

        Account updatePayload = Account.builder()
                .balance(500.0)
                .status(AccountStatus.BLOCKED)
                .holders(List.of("HOLDER-01"))
                .build();

        Account savedAccount = existingAccount.toBuilder()
                .balance(500.0)
                .status(AccountStatus.BLOCKED)
                .holders(List.of("HOLDER-01"))
                .build();

        when(accountPersistencePort.findById(id)).thenReturn(Maybe.just(existingAccount));
        when(accountPersistencePort.save(any(Account.class))).thenReturn(Single.just(savedAccount));

        // When
        TestObserver<Account> testObserver = updateAccountUseCase.execute(id, updatePayload).test();

        // Then
        testObserver.assertValue(account -> account.getId().equals(id)
                && account.getBalance().equals(500.0)
                && account.getStatus() == AccountStatus.BLOCKED);
        testObserver.assertComplete();

        verify(accountPersistencePort).findById(id);
        verify(accountPersistencePort).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit error when target account for update does not exist")
    void execute_WhenAccountDoesNotExist_ShouldEmitIllegalArgumentException() {
        // Given
        String id = "NON-EXISTENT";
        Account updatePayload = Account.builder().balance(300.0).build();

        when(accountPersistencePort.findById(id)).thenReturn(Maybe.empty());

        // When
        TestObserver<Account> testObserver = updateAccountUseCase.execute(id, updatePayload).test();

        // Then
        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("was not found"));

        verify(accountPersistencePort).findById(id);
        verify(accountPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should emit error when account ID is null or blank")
    void execute_WhenIdIsBlank_ShouldEmitIllegalArgumentException() {
        // Given
        Account updatePayload = Account.builder().balance(300.0).build();

        // When
        TestObserver<Account> testObserver = updateAccountUseCase.execute("   ", updatePayload).test();

        // Then
        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("cannot be null or blank"));

        verify(accountPersistencePort, never()).findById(any());
        verify(accountPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("Should emit error when update payload is null")
    void execute_WhenPayloadIsNull_ShouldEmitIllegalArgumentException() {
        // When
        TestObserver<Account> testObserver = updateAccountUseCase.execute("ACC-001", null).test();

        // Then
        testObserver.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("cannot be null"));

        verify(accountPersistencePort, never()).findById(any());
        verify(accountPersistencePort, never()).save(any());
    }
}
