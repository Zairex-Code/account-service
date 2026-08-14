package com.nttdata.bootcamp.account_service.domain.port.input;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import io.reactivex.rxjava3.core.Single;

/**
 * Input Port interface defining the contract for bank account deposit workflows.
 * <p>
 * Technical & Business Rules:
 * - Pure Java domain interface decoupled from Spring Framework, MongoDB or WebFlux controllers.
 * - Credits a positive monetary amount to the account balance.
 * </p>
 */
public interface DepositAccountUseCase {

    /**
     * Deposits a positive monetary amount into an existing bank account.
     *
     * @param id     Unique primary database identifier of the account.
     * @param amount Positive monetary amount to deposit.
     * @return A Single emitting the updated Account domain entity.
     */
    Single<Account> deposit(String id, Double amount);
}
