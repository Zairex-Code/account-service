package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest;


import com.nttdata.bootcamp.account_service.domain.port.input.CreateAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.DeleteAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.GetAccountUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.UpdateAccountUseCase;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.AccountRequestDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.AccountResponseDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.mapper.AccountRestMapper;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive REST Controller exposing HTTP endpoints for bank account management operations.
 * <p>
 * Technical & Business Rules :
 * - Implements Non-Blocking I/O REST API endpoints powered by Spring WebFlux and Netty.
 * - Validates edge request payloads using Jakarta Bean Validation (@Valid).
 * - Delegates business orchestration strictly to input ports (Use Cases).
 * - Converts between HTTP DTOs and domain entities via {@link AccountRestMapper}.
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
    private final AccountRestMapper accountRestMapper;

    /**
     * Creates a new bank account in the core financial system.
     *
     * @param requestDto Validated request payload containing customer and account details.
     * @return A {@link Mono} emitting the created {@link AccountResponseDto} with HTTP 201 Created status.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AccountResponseDto> createAccount(@Valid @RequestBody AccountRequestDto requestDto) {
        log.info("REST request received to create account for customer ID: {}", requestDto.getCustomerId());
        return Mono.just(requestDto)
                .map(accountRestMapper::toDomain)
                .flatMap(createAccountUseCase::execute)
                .map(accountRestMapper::toResponseDto)
                .doOnSuccess(response -> log.info("Account successfully created via REST. ID: {}", response.getId()));
    }

    /**
     * Retrieves an account by its primary database identifier.
     *
     * @param id Account internal primary identifier string.
     * @return A {@link Mono} emitting the matching {@link AccountResponseDto}.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<AccountResponseDto> getAccountById(@PathVariable String id) {
        log.debug("REST request received to fetch account by ID: {}", id);
        return getAccountUseCase.findById(id)
                .map(accountRestMapper::toResponseDto);
    }

    /**
     * Retrieves an account by its official 14-digit public bank account number.
     *
     * @param accountNumber Official unique 14-digit bank account number.
     * @return A {@link Mono} emitting the matching {@link AccountResponseDto}.
     */
    @GetMapping("/number/{accountNumber}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<AccountResponseDto> getAccountByAccountNumber(@PathVariable String accountNumber) {
        log.debug("REST request received to fetch account by account number: {}", accountNumber);
        return getAccountUseCase.findByAccountNumber(accountNumber)
                .map(accountRestMapper::toResponseDto);
    }

    /**
     * Streams all bank accounts associated with a specific customer.
     *
     * @param customerId Unique customer database primary identifier.
     * @return A {@link Flux} streaming matching {@link AccountResponseDto} entities.
     */
    @GetMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<AccountResponseDto> getAccountsByCustomerId(@PathVariable String customerId) {
        log.debug("REST request received to fetch accounts for customer ID: {}", customerId);
        return getAccountUseCase.findByCustomerId(customerId)
                .map(accountRestMapper::toResponseDto);
    }

    /**
     * Streams all registered bank accounts in the core system.
     *
     * @return A {@link Flux} streaming all {@link AccountResponseDto} entities.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<AccountResponseDto> getAllAccounts() {
        log.debug("REST request received to fetch all accounts");
        return getAccountUseCase.findAll()
                .map(accountRestMapper::toResponseDto);
    }

    /**
     * Updates an existing bank account by its primary identifier.
     *
     * @param id         Account primary database identifier string.
     * @param requestDto Validated request payload containing fields to update.
     * @return A {@link Mono} emitting the updated {@link AccountResponseDto}.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<AccountResponseDto> updateAccount(@PathVariable String id,
                                                  @Valid @RequestBody AccountRequestDto requestDto) {
        log.info("REST request received to update account ID: {}", id);
        return Mono.just(requestDto)
                .map(accountRestMapper::toDomain)
                .flatMap(domainAccount -> updateAccountUseCase.execute(id, domainAccount))
                .map(accountRestMapper::toResponseDto)
                .doOnSuccess(response -> log.info("Account ID '{}' successfully updated via REST", id));
    }

    /**
     * Deletes an account from the system by its primary identifier.
     *
     * @param id Account primary database identifier string.
     * @return A {@link Mono} emitting void upon completion with HTTP 204 No Content status.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAccount(@PathVariable String id) {
        log.info("REST request received to delete account ID: {}", id);
        return deleteAccountUseCase.execute(id)
                .doOnSuccess(v -> log.info("Account ID '{}' successfully deleted via REST", id));
    }
}