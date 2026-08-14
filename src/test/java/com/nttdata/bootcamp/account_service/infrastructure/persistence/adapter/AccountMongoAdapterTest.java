package com.nttdata.bootcamp.account_service.infrastructure.persistence.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import com.nttdata.bootcamp.account_service.infrastructure.persistence.document.AccountDocument;
import com.nttdata.bootcamp.account_service.infrastructure.persistence.mapper.AccountPersistenceMapper;
import com.nttdata.bootcamp.account_service.infrastructure.persistence.repository.RxAccountRepository;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test suite for {@link AccountMongoAdapter}.
 */
@ExtendWith(MockitoExtension.class)
class AccountMongoAdapterTest {

    @Mock private RxAccountRepository repository;
    @Mock private AccountPersistenceMapper mapper;

    @InjectMocks
    private AccountMongoAdapter adapter;

    private Account account;
    private AccountDocument document;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id("ACC-001")
                .accountNumber("191-1111111111")
                .customerId("CUST-001")
                .type(AccountType.SAVINGS)
                .build();
        document = AccountDocument.builder()
                .id("ACC-001")
                .accountNumber("191-1111111111")
                .customerId("CUST-001")
                .type(AccountType.SAVINGS)
                .build();
    }

    @Test
    @DisplayName("Should save an account")
    void save_ShouldPersistAccount() {
        when(mapper.toDocument(account)).thenReturn(document);
        when(repository.save(document)).thenReturn(Single.just(document));
        when(mapper.toDomain(document)).thenReturn(account);

        TestObserver<Account> observer = adapter.save(account).test();
        observer.assertValue(account);
        observer.assertComplete();
    }

    @Test
    @DisplayName("Should find account by id")
    void findById_ShouldReturnAccount() {
        when(repository.findById("ACC-001")).thenReturn(Maybe.just(document));
        when(mapper.toDomain(document)).thenReturn(account);

        TestObserver<Account> observer = adapter.findById("ACC-001").test();
        observer.assertValue(account);
    }

    @Test
    @DisplayName("Should find account by account number")
    void findByAccountNumber_ShouldReturnAccount() {
        when(repository.findByAccountNumber("191-1111111111")).thenReturn(Maybe.just(document));
        when(mapper.toDomain(document)).thenReturn(account);

        TestObserver<Account> observer = adapter.findByAccountNumber("191-1111111111").test();
        observer.assertValue(account);
    }

    @Test
    @DisplayName("Should stream accounts by customer id")
    void findByCustomerId_ShouldStreamAccounts() {
        when(repository.findByCustomerId("CUST-001")).thenReturn(Flowable.just(document));
        when(mapper.toDomain(document)).thenReturn(account);

        TestSubscriber<Account> subscriber = adapter.findByCustomerId("CUST-001").test();
        subscriber.assertValue(account);
    }

    @Test
    @DisplayName("Should stream accounts by customer id and type")
    void findByCustomerIdAndType_ShouldStreamAccounts() {
        when(repository.findByCustomerIdAndType("CUST-001", AccountType.SAVINGS))
                .thenReturn(Flowable.just(document));
        when(mapper.toDomain(document)).thenReturn(account);

        TestSubscriber<Account> subscriber =
                adapter.findByCustomerIdAndType("CUST-001", AccountType.SAVINGS).test();
        subscriber.assertValue(account);
    }

    @Test
    @DisplayName("Should stream accounts by customer id and date range")
    void findByCustomerIdAndDateRange_ShouldStreamAccounts() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 0, 0);

        when(repository.findByCustomerIdAndCreatedAtBetween("CUST-001", start, end))
                .thenReturn(Flowable.just(document));
        when(mapper.toDomain(document)).thenReturn(account);

        TestSubscriber<Account> subscriber =
                adapter.findByCustomerIdAndDateRange("CUST-001", start, end).test();
        subscriber.assertValue(account);
    }

    @Test
    @DisplayName("Should stream all accounts")
    void findAll_ShouldStreamAllAccounts() {
        when(repository.findAll()).thenReturn(Flowable.just(document));
        when(mapper.toDomain(document)).thenReturn(account);

        TestSubscriber<Account> subscriber = adapter.findAll().test();
        subscriber.assertValue(account);
    }

    @Test
    @DisplayName("Should delete account by id")
    void deleteById_ShouldComplete() {
        when(repository.deleteById("ACC-001")).thenReturn(Completable.complete());

        adapter.deleteById("ACC-001").test().assertComplete();
        verify(repository).deleteById("ACC-001");
    }
}
