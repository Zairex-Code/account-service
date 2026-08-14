package com.nttdata.bootcamp.account_service.domain.port.input;

import com.nttdata.bootcamp.account_service.domain.model.Account;
import io.reactivex.rxjava3.core.Single;

/**
 * Input Port interface defining the contract for debit card payments.
 */
public interface PayWithDebitCardUseCase {

    /**
     * Debits the account linked to a debit card for a payment.
     *
     * @param cardId Unique internal debit card identifier.
     * @param amount Positive monetary amount to pay.
     * @return A Single emitting the updated Account (debited).
     */
    Single<Account> pay(String cardId, Double amount);
}
