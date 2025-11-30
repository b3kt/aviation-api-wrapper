package com.github.b3kt.aviation.domain.service;

import java.math.BigDecimal;

/**
 * Service for resolving timezone from geographical coordinates.
 */
public interface TimezoneResolver {
    /**
     * Resolves the timezone ID for the given coordinates.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     * @return the timezone ID (e.g., "America/New_York", "UTC", "+05:00")
     */
    String resolve(BigDecimal latitude, BigDecimal longitude);
}
