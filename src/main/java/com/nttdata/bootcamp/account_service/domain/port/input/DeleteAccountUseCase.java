package com.nttdata.bootcamp.account_service.domain.port.input;

import reactor.core.publisher.Mono;

/**
 * Input Port interface defining the contract for bank account deletion and closure workflow
 *
 * Technical & Business Rules:
 * - Pure Java domain interface decoupled from Spring Framework, MongoDB or transport layers.
 * - Enforces pre-deletion validation (Zero balance verification, no pending debits or active holds)
 * - Server as the primary entry point for soft-delete or physical account termination operations.
 */
public interface DeleteAccountUseCase {


    /**
     * Evaluates domain closure conditions and executes account deletion asynchronous
     *
     * @param id Unique primary database identifier of the account to delete
     * @return A Mono emitting void upon successful completion of deletion
     */
    Mono<Void> execute(String id);
}
