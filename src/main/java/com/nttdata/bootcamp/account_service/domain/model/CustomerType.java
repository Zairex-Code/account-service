package com.nttdata.bootcamp.account_service.domain.model;

/**
 * Enumeration representing the customer segment used to enforce account holding limits.
 * <p>
 * Business Rules:
 * - PERSONAL: Individual customer (maximum one savings and one current account).
 * - BUSINESS: Corporate customer (multiple current accounts, no savings or fixed-term).
 * </p>
 */
public enum CustomerType {
    /**
     * Individual / personal customer.
     */
    PERSONAL,

    /**
     * Corporate / business customer.
     */
    BUSINESS
}
