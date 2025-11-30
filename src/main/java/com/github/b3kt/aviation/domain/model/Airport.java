package com.github.b3kt.aviation.domain.model;

import java.math.BigDecimal;

/**
 * Domain entity representing an airport.
 * Immutable record ensuring data consistency.
 */
public record Airport(
        String icaoCode,
        String iataCode,
        String name,
        String city,
        String country,
        BigDecimal latitude,
        BigDecimal longitude,
        String timezone,
        Integer elevation) {
    /**
     * Validates the airport data upon creation.
     */
    public Airport {
        if (icaoCode == null || icaoCode.isBlank()) {
            throw new IllegalArgumentException("ICAO code cannot be null or empty");
        }
        if (!icaoCode.matches("^[A-Z0-9]{4}$")) {
            throw new IllegalArgumentException("ICAO code must be 4 alphanumeric characters");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Airport name cannot be null or empty");
        }
    }

    /**
     * Creates a copy with updated fields (for builder pattern if needed).
     */
    public Airport withIataCode(String iataCode) {
        return new Airport(this.icaoCode, iataCode, this.name, this.city, this.country,
                this.latitude, this.longitude, this.timezone, this.elevation);
    }
}
