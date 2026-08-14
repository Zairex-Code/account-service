package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import com.nttdata.bootcamp.account_service.domain.model.AccountType;
import com.nttdata.bootcamp.account_service.domain.port.input.CreateAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.DeleteAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.DepositAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.GetAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.TransferAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.UpdateAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.WithdrawAccountUseCase;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.AccountRequestDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.AccountResponseDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.TransactionRequestDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.TransferRequestDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.mapper.AccountRestMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test suite for {@link AccountController} (direct instantiation).
 */
@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock private CreateAccountUseCase createAccountUseCase;
    @Mock private GetAccountUseCase getAccountUseCase;
    @Mock private UpdateAccountUseCase updateAccountUseCase;
    @Mock private DeleteAccountUseCase deleteAccountUseCase;
    @Mock private DepositAccountUseCase depositAccountUseCase;
    @Mock private WithdrawAccountUseCase withdrawAccountUseCase;
    @Mock private TransferAccountUseCase transferAccountUseCase;
    @Mock private AccountRestMapper accountRestMapper;

    private AccountController controller;
    private Account domainAccount;
    private AccountResponseDto responseDto;

    @BeforeEach
    void setUp() {
        controller = new AccountController(createAccountUseCase, getAccountUseCase,
                updateAccountUseCase, deleteAccountUseCase, depositAccountUseCase,
                withdrawAccountUseCase, transferAccountUseCase, accountRestMapper);

        domainAccount = Account.builder()
                .id("ACC-001")
                .accountNumber("191-1111111111")
                .customerId("CUST-001")
                .type(AccountType.SAVINGS)
                .balance(100.0)
                .build();

        responseDto = AccountResponseDto.builder()
                .id("ACC-001")
                .accountNumber("191-1111111111")
                .customerId("CUST-001")
                .type(AccountType.SAVINGS)
                .balance(100.0)
                .build();
    }

    @Test
    @DisplayName("Should create an account")
    void createAccount_ShouldReturnResponse() {
        AccountRequestDto request = new AccountRequestDto("CUST-001", AccountType.SAVINGS,
                100.0, null, null, null);

        when(accountRestMapper.toDomain(request)).thenReturn(domainAccount);
        when(createAccountUseCase.execute(domainAccount)).thenReturn(Single.just(domainAccount));
        when(accountRestMapper.toResponseDto(domainAccount)).thenReturn(responseDto);

        TestObserver<AccountResponseDto> observer = controller.createAccount(request).test();

        observer.assertValue(responseDto);
        observer.assertComplete();
    }

    @Test
    @DisplayName("Should get account by id")
    void getAccountById_ShouldReturnResponse() {
        when(getAccountUseCase.findById("ACC-001")).thenReturn(Single.just(domainAccount));
        when(accountRestMapper.toResponseDto(domainAccount)).thenReturn(responseDto);

        TestObserver<AccountResponseDto> observer = controller.getAccountById("ACC-001").test();
        observer.assertValue(responseDto);
    }

    @Test
    @DisplayName("Should get account by number")
    void getAccountByAccountNumber_ShouldReturnResponse() {
        when(getAccountUseCase.findByAccountNumber("191-1111111111"))
                .thenReturn(Maybe.just(domainAccount));
        when(accountRestMapper.toResponseDto(domainAccount)).thenReturn(responseDto);

        TestObserver<AccountResponseDto> observer =
                controller.getAccountByAccountNumber("191-1111111111").test();
        observer.assertValue(responseDto);
    }

    @Test
    @DisplayName("Should stream accounts by customer")
    void getAccountsByCustomerId_ShouldStreamResponse() {
        when(getAccountUseCase.findByCustomerId("CUST-001"))
                .thenReturn(Flowable.just(domainAccount));
        when(accountRestMapper.toResponseDto(domainAccount)).thenReturn(responseDto);

        TestSubscriber<AccountResponseDto> subscriber =
                controller.getAccountsByCustomerId("CUST-001").test();
        subscriber.assertValue(responseDto);
        subscriber.assertComplete();
    }

    @Test
    @DisplayName("Should stream all accounts")
    void getAllAccounts_ShouldStreamResponse() {
        when(getAccountUseCase.findAll()).thenReturn(Flowable.just(domainAccount));
        when(accountRestMapper.toResponseDto(domainAccount)).thenReturn(responseDto);

        TestSubscriber<AccountResponseDto> subscriber = controller.getAllAccounts().test();
        subscriber.assertValue(responseDto);
    }

    @Test
    @DisplayName("Should update an account")
    void updateAccount_ShouldReturnResponse() {
        AccountRequestDto request = new AccountRequestDto("CUST-001", AccountType.SAVINGS,
                100.0, null, null, null);

        when(accountRestMapper.toDomain(request)).thenReturn(domainAccount);
        when(updateAccountUseCase.execute("ACC-001", domainAccount)).thenReturn(Single.just(domainAccount));
        when(accountRestMapper.toResponseDto(domainAccount)).thenReturn(responseDto);

        TestObserver<AccountResponseDto> observer = controller.updateAccount("ACC-001", request).test();
        observer.assertValue(responseDto);
    }

    @Test
    @DisplayName("Should delete an account")
    void deleteAccount_ShouldComplete() {
        when(deleteAccountUseCase.execute("ACC-001")).thenReturn(Completable.complete());

        controller.deleteAccount("ACC-001").test().assertComplete();
        verify(deleteAccountUseCase).execute("ACC-001");
    }

    @Test
    @DisplayName("Should deposit into an account")
    void deposit_ShouldReturnResponse() {
        when(depositAccountUseCase.deposit("ACC-001", 50.0)).thenReturn(Single.just(domainAccount));
        when(accountRestMapper.toResponseDto(domainAccount)).thenReturn(responseDto);

        TestObserver<AccountResponseDto> observer =
                controller.deposit("ACC-001", new TransactionRequestDto(50.0)).test();
        observer.assertValue(responseDto);
    }

    @Test
    @DisplayName("Should withdraw from an account")
    void withdraw_ShouldReturnResponse() {
        when(withdrawAccountUseCase.withdraw("ACC-001", 50.0)).thenReturn(Single.just(domainAccount));
        when(accountRestMapper.toResponseDto(domainAccount)).thenReturn(responseDto);

        TestObserver<AccountResponseDto> observer =
                controller.withdraw("ACC-001", new TransactionRequestDto(50.0)).test();
        observer.assertValue(responseDto);
    }

    @Test
    @DisplayName("Should transfer between accounts")
    void transfer_ShouldReturnResponse() {
        when(transferAccountUseCase.transfer("ACC-001", "ACC-002", 50.0))
                .thenReturn(Single.just(domainAccount));
        when(accountRestMapper.toResponseDto(domainAccount)).thenReturn(responseDto);

        TestObserver<AccountResponseDto> observer =
                controller.transfer("ACC-001", new TransferRequestDto("ACC-002", 50.0)).test();
        observer.assertValue(responseDto);
    }

    @Test
    @DisplayName("Should stream account report by date range")
    void getAccountsReportByDateRange_ShouldStreamResponse() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);

        when(getAccountUseCase.findByCustomerIdAndDateRange(
                eq("CUST-001"), any(), any())).thenReturn(Flowable.just(domainAccount));
        when(accountRestMapper.toResponseDto(domainAccount)).thenReturn(responseDto);

        TestSubscriber<AccountResponseDto> subscriber =
                controller.getAccountsReportByDateRange("CUST-001", start, end).test();
        subscriber.assertValue(responseDto);
        subscriber.assertComplete();

        verify(getAccountUseCase).findByCustomerIdAndDateRange(anyString(), any(), any());
    }
}
