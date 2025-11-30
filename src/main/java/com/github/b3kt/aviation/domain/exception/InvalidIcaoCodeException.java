package com.github.b3kt.aviation.domain.exception;

/**
 * Exception thrown when an invalid ICAO code is provided.
 * This is a domain-level validation exception that will be mapped to HTTP 400.
 */
public class InvalidIcaoCodeException extends RuntimeException {

    private final String icaoCode;

    public InvalidIcaoCodeException(String icaoCode) {
        super(String.format("Invalid ICAO code format: '%s'. ICAO code must be 4 alphanumeric characters", icaoCode));
        this.icaoCode = icaoCode;
    }

    public String getIcaoCode() {
        return icaoCode;
    }
}
