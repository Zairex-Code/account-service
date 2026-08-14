package com.nttdata.bootcamp.account_service.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountStatus;
import com.nttdata.bootcamp.account_service.domain.model.DebitCard;
import com.nttdata.bootcamp.account_service.domain.model.DebitCardStatus;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.DebitCardPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.DomainEventPublisher;
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
 * Unit test suite for {@link PayWithDebitCardUseCaseImpl}.
 */
@ExtendWith(MockitoExtension.class)
class PayWithDebitCardUseCaseImplTest {

    @Mock
    private DebitCardPersistencePort debitCardPersistencePort;

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @Mock
    private MovementClientPort movementClientPort;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private PayWithDebitCardUseCaseImpl payWithDebitCardUseCase;

    private DebitCard activeCard() {
        return DebitCard.builder()
                .id("CARD-001")
                .cardNumber("4500-0000-0000-0001")
                .accountId("ACC-001")
                .status(DebitCardStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should debit linked account when card is active")
    void pay_WhenCardActive_ShouldDebitAccount() {
        Account account = Account.builder()
                .id("ACC-001")
                .balance(500.0)
                .currentMonthlyTransactions(0)
                .status(AccountStatus.ACTIVE)
                .build();

        when(debitCardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(activeCard()));
        when(accountPersistencePort.findById("ACC-001")).thenReturn(Maybe.just(account));
        when(accountPersistencePort.save(any(Account.class)))
                .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
        when(movementClientPort.recordMovement(anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(Completable.complete());
        when(domainEventPublisher.publish(anyString(), any())).thenReturn(Completable.complete());

        TestObserver<Account> observer = payWithDebitCardUseCase.pay("CARD-001", 100.0).test();

        observer.assertValue(result -> result.getBalance().equals(400.0)
                && result.getCurrentMonthlyTransactions() == 1);
        observer.assertComplete();

        verify(accountPersistencePort).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit error when card does not exist")
    void pay_WhenCardNotFound_ShouldEmitIllegalArgumentException() {
        when(debitCardPersistencePort.findById("NON-EXISTENT")).thenReturn(Maybe.empty());

        TestObserver<Account> observer = payWithDebitCardUseCase.pay("NON-EXISTENT", 100.0).test();

        observer.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("was not found"));

        verify(accountPersistencePort, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit error when card is blocked")
    void pay_WhenCardBlocked_ShouldEmitIllegalStateException() {
        DebitCard blocked = activeCard().toBuilder().status(DebitCardStatus.BLOCKED).build();

        when(debitCardPersistencePort.findById("CARD-001")).thenReturn(Maybe.just(blocked));

        TestObserver<Account> observer = payWithDebitCardUseCase.pay("CARD-001", 100.0).test();

        observer.assertError(throwable -> throwable instanceof IllegalStateException
                && throwable.getMessage().contains("not ACTIVE"));

        verify(accountPersistencePort, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Should emit error when amount is not positive")
    void pay_WhenAmountNotPositive_ShouldEmitIllegalArgumentException() {
        TestObserver<Account> observer = payWithDebitCardUseCase.pay("CARD-001", 0.0).test();

        observer.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("greater than zero"));

        verify(debitCardPersistencePort, never()).findById(anyString());
    }
}
