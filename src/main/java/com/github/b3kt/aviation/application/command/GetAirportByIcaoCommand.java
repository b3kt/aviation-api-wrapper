package com.github.b3kt.aviation.application.command;

import com.github.b3kt.aviation.application.dto.AirportResponse;
import com.github.b3kt.aviation.domain.exception.InvalidIcaoCodeException;

/**
 * Command for retrieving airport information by ICAO code.
 * Immutable record with validation.
 */
public record GetAirportByIcaoCommand(String icaoCode) implements Command<AirportResponse> {

    /**
     * Validates ICAO code format upon construction.
     */
    public GetAirportByIcaoCommand {
        if (icaoCode == null || icaoCode.isBlank()) {
            throw new InvalidIcaoCodeException(icaoCode);
        }

        // Normalize to uppercase
        icaoCode = icaoCode.trim().toUpperCase();

        // Validate format: 4 alphanumeric characters
        if (!icaoCode.matches("^[A-Z0-9]{4}$")) {
            throw new InvalidIcaoCodeException(icaoCode);
        }
    }
}
