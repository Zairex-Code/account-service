package com.nttdata.bootcamp.account_service.domain.model;


/**
 * Enumeration representing the passive bank account product type supported by the core engine
 *
 * Business Rules (Financial Domain Standards)
 * - SAVINGS: Free maintenance fee, subject to a maximum limit of monthly transaction
 * - CURRENT: Incurs a monthly maintenance fee, unlimited monthly transactions
 * - FIXED_TERM: Free maintenance fee, permits deposit or withdrawal strictly on a specific day of the month.
 */
public enum AccountType {
    /**
     * Savings account: Zero maintenance fee, restricted monthly transaction count.
     */
    SAVINGS,

    /**
     * Current (checking) account: Monthly maintenance fee applies, unlimited transactions.
     */
    CURRENT,

    /**
     * Fixed-term deposit account: Zero maintenance fee, single transaction day per month.
     */
    FIXED_TERM
}
