package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest;


import com.nttdata.bootcamp.account_service.domain.exception.InsufficientBalanceException;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.ErrorResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global reactive exception handler providing centralized RFC 7807 error responses for the REST API.
 * <p>
 * Technical & Business Rules:
 * - Intercepts domain and framework exceptions across WebFlux reactive pipelines.
 * - Converts uncaught exceptions into standardized immutable {@link ErrorResponseDto} records.
 * - Extracts field-level validation messages when Jakarta @Valid fails on incoming DTO records.
 * - Protects internal JVM stack traces from leaking to public REST consumers (DevSecOps compliant).
 * </p>

 * @author NTT DATA Bootcamp Team
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles illegal argument exceptions thrown by domain use cases or validation logic.
     *
     * @param ex       Captured {@link IllegalArgumentException}.
     * @param exchange Current reactive HTTP exchange context.
     * @return A {@link Mono} emitting a {@link ResponseEntity} with HTTP 400 Bad Request status.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleIllegalArgumentException(
            IllegalArgumentException ex, ServerWebExchange exchange) {
        log.warn("Business validation rule violated: {}", ex.getMessage());

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    /**
     * Handles illegal state exceptions (e.g., attempting account closure with a non-zero balance).
     *
     * @param ex       Captured {@link IllegalStateException}.
     * @param exchange Current reactive HTTP exchange context.
     * @return A {@link Mono} emitting a {@link ResponseEntity} with HTTP 409 Conflict status.
     */
    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleIllegalStateException(
            IllegalStateException ex, ServerWebExchange exchange) {
        log.warn("Operational state constraint violated: {}", ex.getMessage());

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
    }

    /**
     * Handles custom domain exception when an account has insufficient funds for an operation.
     *
     * @param ex       Captured {@link InsufficientBalanceException}.
     * @param exchange Current reactive HTTP exchange context.
     * @return A {@link Mono} emitting a {@link ResponseEntity} with HTTP 422 Unprocessable Entity status.
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleInsufficientBalanceException(
            InsufficientBalanceException ex, ServerWebExchange exchange) {
        log.warn("Insufficient funds domain exception triggered: {}", ex.getMessage());

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse));
    }

    /**
     * Handles WebFlux DTO validation errors triggered by Jakarta @Valid on REST endpoints.
     *
     * @param ex       Captured {@link WebExchangeBindException}.
     * @param exchange Current reactive HTTP exchange context.
     * @return A {@link Mono} emitting a {@link ResponseEntity} with HTTP 400 Bad Request status.
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleWebExchangeBindException(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        log.warn("DTO request validation failed for path: {}", exchange.getRequest().getPath().value());

        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Request payload validation failed")
                .path(exchange.getRequest().getPath().value())
                .details(details)
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    /**
     * Fallback handler for all unexpected or unhandled system exceptions.
     *
     * @param ex       Captured generic {@link Exception}.
     * @param exchange Current reactive HTTP exchange context.
     * @return A {@link Mono} emitting a {@link ResponseEntity} with HTTP 500 Internal Server Error status.
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        log.error("Unhandled internal server exception encountered", ex);

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected internal server error occurred. Please contact system support.")
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }
}