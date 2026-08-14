package com.nttdata.bootcamp.account_service.domain.model;

/**
 * Enumeration representing the commercial profile of a bank customer.
 * <p>
 * Business Rules:
 * - STANDARD: Default tier without special conditions.
 * - VIP: Premium personal tier requiring an active credit card.
 * - PYME: Premium business tier requiring an active credit card.
 * </p>
 */
public enum CustomerProfile {
    /**
     * Default commercial tier.
     */
    STANDARD,

    /**
     * Premium personal tier.
     */
    VIP,

    /**
     * Premium business tier.
     */
    PYME
}
