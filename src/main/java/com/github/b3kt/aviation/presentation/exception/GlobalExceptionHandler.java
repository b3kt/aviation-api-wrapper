package com.github.b3kt.aviation.presentation.exception;

import com.github.b3kt.aviation.domain.exception.AirportNotFoundException;
import com.github.b3kt.aviation.domain.exception.InvalidIcaoCodeException;
import com.github.b3kt.aviation.presentation.dto.ErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import java.util.concurrent.TimeoutException;

/**
 * Global exception handler for consistent error responses.
 * Maps domain exceptions to appropriate HTTP status codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AirportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAirportNotFound(
            AirportNotFoundException ex,
            ServerWebExchange exchange) {
        log.warn("Airport not found: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Airport Not Found",
                ex.getMessage(),
                exchange.getRequest().getPath().value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidIcaoCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidIcaoCode(
            InvalidIcaoCodeException ex,
            ServerWebExchange exchange) {
        log.warn("Invalid ICAO code: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request",
                ex.getMessage(),
                exchange.getRequest().getPath().value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCircuitBreakerOpen(
            CallNotPermittedException ex,
            ServerWebExchange exchange) {
        log.error("Circuit breaker is open: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "The aviation data service is currently unavailable. Please try again later.",
                exchange.getRequest().getPath().value());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RequestNotPermitted ex,
            ServerWebExchange exchange) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                "Rate limit exceeded. Please try again later.",
                exchange.getRequest().getPath().value());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(
            TimeoutException ex,
            ServerWebExchange exchange) {
        log.error("Request timeout: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.GATEWAY_TIMEOUT.value(),
                "Gateway Timeout",
                "The request timed out. Please try again.",
                exchange.getRequest().getPath().value());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(error);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientException(
            WebClientResponseException ex,
            ServerWebExchange exchange) {
        log.error("External API error: {} - {}", ex.getStatusCode(), ex.getMessage());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_GATEWAY.value(),
                "Bad Gateway",
                "Error communicating with aviation data service.",
                exchange.getRequest().getPath().value());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            ServerWebExchange exchange) {
        log.error("Unexpected error: ", ex);
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                exchange.getRequest().getPath().value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
