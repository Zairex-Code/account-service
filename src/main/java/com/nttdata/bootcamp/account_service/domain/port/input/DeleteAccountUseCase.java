package com.nttdata.bootcamp.account_service.domain.port.input;

import io.reactivex.rxjava3.core.Completable;

/**
 * Input Port interface defining the contract for bank account deletion and closure workflow
 *
 * Technical & Business Rules:
 * - Pure Java domain interface decoupled from Spring Framework, MongoDB or transport layers.
 * - Enforces pre-deletion validation (Zero balance verification, no pending debits or active holds)
 * - Serves as the primary entry point for soft-delete or physical account termination operations.
 */
public interface DeleteAccountUseCase {


    /**
     * Evaluates domain closure conditions and executes account deletion asynchronous
     *
     * @param id Unique primary database identifier of the account to delete
     * @return A Completable that completes upon successful completion of deletion
     */
    Completable execute(String id);
}
