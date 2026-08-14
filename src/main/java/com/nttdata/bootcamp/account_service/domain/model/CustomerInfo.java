package com.nttdata.bootcamp.account_service.domain.model;

/**
 * Immutable snapshot of the customer data required by account-service to enforce holding limits.
 * <p>
 * Technical & Business Rules:
 * - Pure Java record decoupled from the customer-service transport and persistence layers.
 * - Exposes the minimal customer context (type and profile) used by account business rules.
 * </p>
 *
 * @param id      Unique customer primary database identifier.
 * @param type    Customer segment (PERSONAL or BUSINESS).
 * @param profile Commercial profile (STANDARD, VIP, PYME).
 */
public record CustomerInfo(
        String id,
        CustomerType type,
        CustomerProfile profile
) {
}
