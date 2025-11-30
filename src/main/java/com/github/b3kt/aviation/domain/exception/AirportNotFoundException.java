package com.github.b3kt.aviation.domain.exception;

/**
 * Exception thrown when an airport cannot be found by ICAO code.
 * This is a domain-level exception that will be mapped to HTTP 404 in the
 * presentation layer.
 */
public class AirportNotFoundException extends RuntimeException {

    private final String icaoCode;

    public AirportNotFoundException(String icaoCode) {
        super(String.format("Airport with ICAO code '%s' not found", icaoCode));
        this.icaoCode = icaoCode;
    }

    public String getIcaoCode() {
        return icaoCode;
    }
}
