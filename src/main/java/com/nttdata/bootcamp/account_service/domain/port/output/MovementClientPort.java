package com.nttdata.bootcamp.account_service.domain.port.output;

import io.reactivex.rxjava3.core.Completable;

/**
 * Secondary Output Port interface defining the contract for recording monetary movements
 * in the external transaction-service.
 * <p>
 * Technical & Business Rules:
 * - Pure Java domain interface decoupled from WebClient or HTTP framework dependencies.
 * - Enables account-service to register deposits and withdrawals in the movement ledger.
 * - Enforces non-blocking reactive communication between microservices.
 * </p>
 */
public interface MovementClientPort {

    /**
     * Records a monetary movement in the transaction-service ledger.
     *
     * @param productId    Unique product database identifier (account ID).
     * @param productType  Bank product type (e.g., ACCOUNT).
     * @param movementType Type of movement (e.g., DEPOSIT, WITHDRAWAL).
     * @param amount       Monetary amount of the movement.
     * @return A Completable that completes when the movement has been recorded.
     */
    Completable recordMovement(String productId, String productType, String movementType, Double amount);
}
