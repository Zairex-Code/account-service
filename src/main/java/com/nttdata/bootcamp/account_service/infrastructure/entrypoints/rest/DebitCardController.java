package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest;

import com.nttdata.bootcamp.account_service.domain.port.input.IssueDebitCardUseCase;
import com.nttdata.bootcamp.account_service.domain.port.input.PayWithDebitCardUseCase;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.AccountResponseDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.DebitCardRequestDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.DebitCardResponseDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.TransactionRequestDto;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.mapper.AccountRestMapper;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.mapper.DebitCardRestMapper;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reactive REST Controller exposing debit card operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/debit-cards")
@RequiredArgsConstructor
public class DebitCardController {

    private final IssueDebitCardUseCase issueDebitCardUseCase;
    private final PayWithDebitCardUseCase payWithDebitCardUseCase;
    private final DebitCardRestMapper debitCardRestMapper;
    private final AccountRestMapper accountRestMapper;

    /**
     * Issues a debit card linked to an existing account.
     *
     * @param requestDto Validated request payload containing the owning account ID.
     * @return A {@link Single} emitting the issued {@link DebitCardResponseDto}.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Single<DebitCardResponseDto> issueDebitCard(@Valid @RequestBody DebitCardRequestDto requestDto) {
        log.info("REST request received to issue debit card for account ID: {}", requestDto.accountId());
        return issueDebitCardUseCase.issue(requestDto.accountId())
                .map(debitCardRestMapper::toResponseDto)
                .doOnSuccess(response -> log.info("Debit card issued via REST. ID: {}", response.id()));
    }

    /**
     * Pays with a debit card, debiting the linked account.
     *
     * @param id         Debit card primary database identifier.
     * @param requestDto Validated request payload containing the payment amount.
     * @return A {@link Single} emitting the debited {@link AccountResponseDto}.
     */
    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.OK)
    public Single<AccountResponseDto> pay(@PathVariable String id,
                                          @Valid @RequestBody TransactionRequestDto requestDto) {
        log.info("REST request received to pay with debit card ID: {}", id);
        return payWithDebitCardUseCase.pay(id, requestDto.amount())
                .map(accountRestMapper::toResponseDto)
                .doOnSuccess(response -> log.info("Debit card payment applied via REST. Card ID: {}", id));
    }
}
