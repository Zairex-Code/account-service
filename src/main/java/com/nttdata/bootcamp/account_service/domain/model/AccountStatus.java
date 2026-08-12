package com.nttdata.bootcamp.account_service.domain.model;

/**
 * Enumeration defining the operational lifecycle states of a bank account in the core engine.
 * <p>
 * Business Rules (BCP Standards):
 * - ACTIVE: Fully operational for deposits, withdrawals, and transfers.
 * - BLOCKED: Locked due to security alerts, legal hold, or overdue credit debts.
 * - INACTIVE: Temporarily disabled due to extended zero-movement periods.
 * - CLOSED: Permanently terminated account instance.
 * </p>
 */
public enum AccountStatus {
    ACTIVE,
    BLOCKED,
    INACTIVE,
    CLOSED
}
