package com.nttdata.bootcamp.account_service.domain.port.input;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import io.reactivex.rxjava3.core.Single;

/**
 * Input Port interface defining the contract for bank account withdrawal workflows.
 * <p>
 * Technical & Business Rules:
 * - Pure Java domain interface decoupled from Spring Framework, MongoDB or WebFlux controllers.
 * - Debits a positive monetary amount from the account balance, enforcing balance availability.
 * </p>
 */
public interface WithdrawAccountUseCase {

    /**
     * Withdraws a positive monetary amount from an existing bank account.
     *
     * @param id     Unique primary database identifier of the account.
     * @param amount Positive monetary amount to withdraw.
     * @return A Single emitting the updated Account domain entity.
     */
    Single<Account> withdraw(String id, Double amount);
}
