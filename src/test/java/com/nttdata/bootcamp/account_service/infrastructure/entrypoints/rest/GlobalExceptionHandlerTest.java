package com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nttdata.bootcamp.account_service.domain.exception.InsufficientBalanceException;
import com.nttdata.bootcamp.account_service.infrastructure.entrypoints.rest.dto.ErrorResponseDto;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

/**
 * Unit test suite for {@link GlobalExceptionHandler}.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        RequestPath path = mock(RequestPath.class);
        when(path.value()).thenReturn("/api/v1/accounts");

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getPath()).thenReturn(path);

        exchange = mock(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);
    }

    @Test
    @DisplayName("Should return 400 for IllegalArgumentException")
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        Single<ResponseEntity<ErrorResponseDto>> result =
                handler.handleIllegalArgumentException(new IllegalArgumentException("bad"), exchange);

        TestObserver<ResponseEntity<ErrorResponseDto>> observer = result.test();
        observer.assertComplete();
        assertEquals(HttpStatus.BAD_REQUEST.value(), observer.values().get(0).getBody().status());
    }

    @Test
    @DisplayName("Should return 409 for IllegalStateException")
    void handleIllegalStateException_ShouldReturnConflict() {
        Single<ResponseEntity<ErrorResponseDto>> result =
                handler.handleIllegalStateException(new IllegalStateException("conflict"), exchange);

        TestObserver<ResponseEntity<ErrorResponseDto>> observer = result.test();
        observer.assertComplete();
        assertEquals(HttpStatus.CONFLICT.value(), observer.values().get(0).getBody().status());
    }

    @Test
    @DisplayName("Should return 422 for InsufficientBalanceException")
    void handleInsufficientBalanceException_ShouldReturnUnprocessable() {
        Single<ResponseEntity<ErrorResponseDto>> result =
                handler.handleInsufficientBalanceException(
                        new InsufficientBalanceException("no funds"), exchange);

        TestObserver<ResponseEntity<ErrorResponseDto>> observer = result.test();
        observer.assertComplete();
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), observer.values().get(0).getBody().status());
    }

    @Test
    @DisplayName("Should return 400 for WebExchangeBindException")
    void handleWebExchangeBindException_ShouldReturnBadRequest() {
        FieldError fieldError = mock(FieldError.class);
        when(fieldError.getDefaultMessage()).thenReturn("required");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        Single<ResponseEntity<ErrorResponseDto>> result =
                handler.handleWebExchangeBindException(ex, exchange);

        TestObserver<ResponseEntity<ErrorResponseDto>> observer = result.test();
        observer.assertComplete();
        assertEquals(HttpStatus.BAD_REQUEST.value(), observer.values().get(0).getBody().status());
    }

    @Test
    @DisplayName("Should return 500 for generic Exception")
    void handleGenericException_ShouldReturnInternalServerError() {
        Single<ResponseEntity<ErrorResponseDto>> result =
                handler.handleGenericException(new RuntimeException("boom"), exchange);

        TestObserver<ResponseEntity<ErrorResponseDto>> observer = result.test();
        observer.assertComplete();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                observer.values().get(0).getBody().status());
    }
}
