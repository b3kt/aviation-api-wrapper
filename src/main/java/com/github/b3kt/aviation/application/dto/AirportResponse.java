package com.github.b3kt.aviation.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.b3kt.aviation.domain.model.Airport;

import java.math.BigDecimal;

/**
 * Output DTO for airport information.
 * Separate from domain model to allow API versioning flexibility.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AirportResponse(
        String icaoCode,
        String iataCode,
        String name,
        String city,
        String country,
        Coordinates coordinates,
        String timezone,
        Integer elevationFeet) {

    /**
     * Nested record for geographic coordinates.
     */
    public record Coordinates(
            BigDecimal latitude,
            BigDecimal longitude) {
    }

    /**
     * Factory method to create response from domain entity.
     * 
     * @param airport the domain airport entity
     * @return the response DTO
     */
    public static AirportResponse fromDomain(Airport airport) {
        Coordinates coordinates = null;
        if (airport.latitude() != null && airport.longitude() != null) {
            coordinates = new Coordinates(airport.latitude(), airport.longitude());
        }

        return new AirportResponse(
                airport.icaoCode(),
                airport.iataCode(),
                airport.name(),
                airport.city(),
                airport.country(),
                coordinates,
                airport.timezone(),
                airport.elevation());
    }
}
