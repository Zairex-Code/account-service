package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest;


import com.nttdata.bootcamp.account_service.domain.port.input.CreateAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.DeleteAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.DepositAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.GetAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.UpdateAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.WithdrawAccountUseCase;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.AccountRequestDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.AccountResponseDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.TransactionRequestDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.mapper.AccountRestMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reactive REST Controller exposing HTTP endpoints for bank account management operations.
 * <p>
 * Technical & Business Rules :
 * - Implements Non-Blocking I/O REST API endpoints powered by Spring WebFlux and Netty.
 * - Validates edge request payloads using Jakarta Bean Validation (@Valid).
 * - Delegates business orchestration strictly to input ports (Use Cases).
 * - Converts between HTTP DTOs and domain entities via {@link AccountRestMapper}.
 * - Non-blocking execution using RxJava 3 (Single / Maybe / Flowable / Completable).
 * </p>

 */
@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final DepositAccountUseCase depositAccountUseCase;
    private final WithdrawAccountUseCase withdrawAccountUseCase;
    private final AccountRestMapper accountRestMapper;

    /**
     * Creates a new bank account in the core financial system.
     *
     * @param requestDto Validated request payload containing customer and account details.
     * @return A {@link Single} emitting the created {@link AccountResponseDto} with HTTP 201 Created status.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Single<AccountResponseDto> createAccount(@Valid @RequestBody AccountRequestDto requestDto) {
        log.info("REST request received to create account for customer ID: {}", requestDto.customerId());
        return Single.just(requestDto)
                .map(accountRestMapper::toDomain)
                .flatMap(createAccountUseCase::execute)
                .map(accountRestMapper::toResponseDto)
                .doOnSuccess(response -> log.info(
                        "Account successfully created via REST. ID: {}", response.id()));
    }

    /**
     * Retrieves an account by its primary database identifier.
     *
     * @param id Account internal primary identifier string.
     * @return A {@link Single} emitting the matching {@link AccountResponseDto}.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Single<AccountResponseDto> getAccountById(@PathVariable String id) {
        log.debug("REST request received to fetch account by ID: {}", id);
        return getAccountUseCase.findById(id)
                .map(accountRestMapper::toResponseDto);
    }

    /**
     * Retrieves an account by its official 14-digit public bank account number.
     *
     * @param accountNumber Official unique 14-digit bank account number.
     * @return A {@link Maybe} emitting the matching {@link AccountResponseDto}.
     */
    @GetMapping("/number/{accountNumber}")
    @ResponseStatus(HttpStatus.OK)
    public Maybe<AccountResponseDto> getAccountByAccountNumber(@PathVariable String accountNumber) {
        log.debug("REST request received to fetch account by account number: {}", accountNumber);
        return getAccountUseCase.findByAccountNumber(accountNumber)
                .map(accountRestMapper::toResponseDto);
    }

    /**
     * Streams all bank accounts associated with a specific customer.
     *
     * @param customerId Unique customer database primary identifier.
     * @return A {@link Flowable} streaming matching {@link AccountResponseDto} entities.
     */
    @GetMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    public Flowable<AccountResponseDto> getAccountsByCustomerId(@PathVariable String customerId) {
        log.debug("REST request received to fetch accounts for customer ID: {}", customerId);
        return getAccountUseCase.findByCustomerId(customerId)
                .map(accountRestMapper::toResponseDto);
    }

    /**
     * Streams all registered bank accounts in the core system.
     *
     * @return A {@link Flowable} streaming all {@link AccountResponseDto} entities.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flowable<AccountResponseDto> getAllAccounts() {
        log.debug("REST request received to fetch all accounts");
        return getAccountUseCase.findAll()
                .map(accountRestMapper::toResponseDto);
    }

    /**
     * Updates an existing bank account by its primary identifier.
     *
     * @param id         Account primary database identifier string.
     * @param requestDto Validated request payload containing fields to update.
     * @return A {@link Single} emitting the updated {@link AccountResponseDto}.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Single<AccountResponseDto> updateAccount(@PathVariable String id,
                                                    @Valid @RequestBody AccountRequestDto requestDto) {
        log.info("REST request received to update account ID: {}", id);
        return Single.just(requestDto)
                .map(accountRestMapper::toDomain)
                .flatMap(domainAccount -> updateAccountUseCase.execute(id, domainAccount))
                .map(accountRestMapper::toResponseDto)
                .doOnSuccess(response -> log.info(
                        "Account ID '{}' successfully updated via REST", id));
    }

    /**
     * Deletes an account from the system by its primary identifier.
     *
     * @param id Account primary database identifier string.
     * @return A {@link Completable} that completes upon success with HTTP 204 No Content status.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Completable deleteAccount(@PathVariable String id) {
        log.info("REST request received to delete account ID: {}", id);
        return deleteAccountUseCase.execute(id)
                .doOnComplete(() -> log.info("Account ID '{}' successfully deleted via REST", id));
    }

    /**
     * Deposits a positive monetary amount into an account.
     *
     * @param id         Account primary database identifier string.
     * @param requestDto Validated request payload containing the deposit amount.
     * @return A {@link Single} emitting the updated {@link AccountResponseDto}.
     */
    @PostMapping("/{id}/deposits")
    @ResponseStatus(HttpStatus.OK)
    public Single<AccountResponseDto> deposit(@PathVariable String id,
                                               @Valid @RequestBody TransactionRequestDto requestDto) {
        log.info("REST request received to deposit into account ID: {}", id);
        return depositAccountUseCase.deposit(id, requestDto.amount())
                .map(accountRestMapper::toResponseDto)
                .doOnSuccess(response -> log.info("Deposit applied via REST to account ID: {}", id));
    }

    /**
     * Withdraws a positive monetary amount from an account.
     *
     * @param id         Account primary database identifier string.
     * @param requestDto Validated request payload containing the withdrawal amount.
     * @return A {@link Single} emitting the updated {@link AccountResponseDto}.
     */
    @PostMapping("/{id}/withdrawals")
    @ResponseStatus(HttpStatus.OK)
    public Single<AccountResponseDto> withdraw(@PathVariable String id,
                                                @Valid @RequestBody TransactionRequestDto requestDto) {
        log.info("REST request received to withdraw from account ID: {}", id);
        return withdrawAccountUseCase.withdraw(id, requestDto.amount())
                .map(accountRestMapper::toResponseDto)
                .doOnSuccess(response -> log.info("Withdrawal applied via REST to account ID: {}", id));
    }
}
