package com.nttdata.bootcamp.account_service.domain.port.input;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import io.reactivex.rxjava3.core.Single;


/**
 * Input Port interface defining the contract for bank account creation workflows
 *
 * Technical & Business Rules:
 * - Pure Java domain interface decoupled from Framework, MongoDB or WebFlux controllers
 * - Serves as the primary entry point for evaluating and opening financial bank account
 * - Enforces product holding limits per customer type (Personal vs Business)
 * - Validate minimum opening balance requirements based on customer profile (STANDARD, VIP , PYME)
 */
public interface CreateAccountUseCase {

    /**
     * Evaluates domain business rules and creates a new bank account asynchronously
     *
     * @param account Domain entity instance containing the requested account parameters
     * @return A Single emitting the newly persisted Account domain entity.
     */
    Single<Account> execute(Account account);
}
