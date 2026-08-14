package com.nttdata.bootcamp.account_service.domain.port.input;

import com.nttdata.bootcamp.account_service.domain.model.DebitCard;
import io.reactivex.rxjava3.core.Single;

/**
 * Input Port interface defining the contract for issuing debit cards.
 */
public interface IssueDebitCardUseCase {

    /**
     * Issues a debit card linked to an existing bank account.
     *
     * @param accountId Unique primary database identifier of the owning account.
     * @return A Single emitting the issued DebitCard.
     */
    Single<DebitCard> issue(String accountId);
}
