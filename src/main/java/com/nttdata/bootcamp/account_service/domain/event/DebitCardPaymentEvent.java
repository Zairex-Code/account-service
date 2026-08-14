package com.nttdata.bootcamp.account_service.domain.event;

/**
 * Domain event emitted when a debit card payment is applied.
 *
 * @param accountId Unique identifier of the debited account.
 * @param cardId    Unique identifier of the debit card used.
 * @param amount    Monetary amount of the payment.
 * @param timestamp Epoch milliseconds when the event occurred.
 */
public record DebitCardPaymentEvent(
        String accountId,
        String cardId,
        Double amount,
        long timestamp
) {
}
