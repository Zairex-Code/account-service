package com.nttdata.bootcamp.account_service.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.DebitCard;
import com.nttdata.bootcamp.account_service.domain.model.DebitCardStatus;
import com.nttdata.bootcamp.account_service.domain.port.output.AccountPersistencePort;
import com.nttdata.bootcamp.account_service.domain.port.output.DebitCardPersistencePort;
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
 * Unit test suite for {@link IssueDebitCardUseCaseImpl}.
 */
@ExtendWith(MockitoExtension.class)
class IssueDebitCardUseCaseImplTest {

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @Mock
    private DebitCardPersistencePort debitCardPersistencePort;

    @InjectMocks
    private IssueDebitCardUseCaseImpl issueDebitCardUseCase;

    @Test
    @DisplayName("Should issue a debit card when account exists")
    void issue_WhenAccountExists_ShouldReturnCard() {
        Account account = Account.builder().id("ACC-001").build();

        when(accountPersistencePort.findById("ACC-001")).thenReturn(Maybe.just(account));
        when(debitCardPersistencePort.save(any(DebitCard.class)))
                .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

        TestObserver<DebitCard> observer = issueDebitCardUseCase.issue("ACC-001").test();

        observer.assertValue(card -> card.getAccountId().equals("ACC-001")
                && card.getStatus() == DebitCardStatus.ACTIVE
                && card.getCardNumber() != null);
        observer.assertComplete();

        verify(debitCardPersistencePort).save(any(DebitCard.class));
    }

    @Test
    @DisplayName("Should emit error when account does not exist")
    void issue_WhenAccountNotFound_ShouldEmitIllegalArgumentException() {
        when(accountPersistencePort.findById("NON-EXISTENT")).thenReturn(Maybe.empty());

        TestObserver<DebitCard> observer = issueDebitCardUseCase.issue("NON-EXISTENT").test();

        observer.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("was not found"));

        verify(debitCardPersistencePort, never()).save(any(DebitCard.class));
    }

    @Test
    @DisplayName("Should emit error when account ID is blank")
    void issue_WhenAccountIdBlank_ShouldEmitIllegalArgumentException() {
        TestObserver<DebitCard> observer = issueDebitCardUseCase.issue("  ").test();

        observer.assertError(throwable -> throwable instanceof IllegalArgumentException
                && throwable.getMessage().contains("Account ID"));

        verify(accountPersistencePort, never()).findById(any());
    }
}
