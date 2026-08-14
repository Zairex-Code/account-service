package com.nttdata.bootcamp.account_service.domain.port.input;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import io.reactivex.rxjava3.core.Single;

/**
 * Input Port interface defining the contract for account-to-account transfer workflows.
 * <p>
 * Technical & Business Rules:
 * - Pure Java domain interface decoupled from Spring Framework, MongoDB or WebFlux controllers.
 * - Transfers a positive monetary amount between two accounts within the same bank.
 * </p>
 */
public interface TransferAccountUseCase {

    /**
     * Transfers a positive monetary amount from a source account to a destination account.
     *
     * @param sourceId      Unique primary database identifier of the source account.
     * @param destinationId Unique primary database identifier of the destination account.
     * @param amount        Positive monetary amount to transfer.
     * @return A Single emitting the updated source Account domain entity.
     */
    Single<Account> transfer(String sourceId, String destinationId, Double amount);
}
