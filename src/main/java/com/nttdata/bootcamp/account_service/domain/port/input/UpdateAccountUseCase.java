package com.nttdata.bootcamp.account_service.domain.port.input;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import io.reactivex.rxjava3.core.Single;

/**
 * Input Port interface defining the contract for updating existing bank account records
 *
 * Technical & Business Rules:
 * - Pure Java domain interface decoupled from Spring Framework, MongoDB or WebFlux controllers.
 * - Enforces customer existence and state transition checks prior to performing updates
 * - Protects immutable account attributes (account number customer ID, creation timestamp)
 * - Allows updates to modifiable parameters such as operational status, holders, and signatories.
 *
 */
public interface UpdateAccountUseCase {

    /**
     * Evaluates domain business rules and updates an existing bank account asynchronously
     *
     * @param id Unique primary database identifier of the account to update
     * @param account Account Domain entity instance containing updated attribute value
     * @return A Single emitting the updated Account domain entity
     */
    Single<Account> execute(String id, Account account);
}
