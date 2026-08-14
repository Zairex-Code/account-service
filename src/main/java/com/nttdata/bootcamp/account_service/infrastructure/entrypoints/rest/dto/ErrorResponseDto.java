package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto;



import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * Data Transfer Object representing a standardized RFC 7807 HTTP REST error response body.
 * <p>
 * Technical & Business Rules (NTT DATA / Banking Standards):
 * - Implemented as an immutable Java 17 Record for thread-safe error reporting across REST endpoints.
 * - Standardizes error structures across all financial microservices (RFC 7807 Problem Details).
 * - Prevents leakage of internal JVM stack traces to public API consumers.
 * - Supports Lombok @Builder on records for fluent error construction inside exception handlers.
 * </p>
 *
 * @author NTT DATA Bootcamp Team
 * @version 1.0
 */
@Builder
public record ErrorResponseDto(

        /**
         * Exact timestamp when the exception occurred.
         */
        LocalDateTime timestamp,

        /**
         * HTTP status code number (e.g., 400, 404, 409, 500).
         */
        Integer status,

        /**
         * Short name or classification of the HTTP error (e.g., "Bad Request", "Not Found").
         */
        String error,

        /**
         * Primary human-readable description explaining the cause of the failure.
         */
        String message,

        /**
         * Request URI path that originated the exception (e.g., "/api/v1/accounts").
         */
        String path,

        /**
         * Optional list of specific field validation errors (used for Jakarta @Valid failures).
         */
        List<String> details
) {
}
