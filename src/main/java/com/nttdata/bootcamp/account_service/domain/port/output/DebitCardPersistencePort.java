package com.nttdata.bootcamp.account_service.domain.port.output;

import com.nttdata.bootcamp.account_service.domain.model.DebitCard;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Secondary Output Port interface for debit card persistence operations.
 */
public interface DebitCardPersistencePort {

    /**
     * Persists a new debit card.
     *
     * @param debitCard Domain entity to save.
     * @return A Single emitting the saved DebitCard.
     */
    Single<DebitCard> save(DebitCard debitCard);

    /**
     * Retrieves a debit card by its internal primary identifier.
     *
     * @param id Unique internal debit card identifier.
     * @return A Maybe emitting the matching DebitCard, or empty if not found.
     */
    Maybe<DebitCard> findById(String id);
}
