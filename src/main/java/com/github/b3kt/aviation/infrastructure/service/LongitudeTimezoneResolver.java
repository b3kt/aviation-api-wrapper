package com.github.b3kt.aviation.infrastructure.service;

import com.github.b3kt.aviation.domain.service.TimezoneResolver;
import com.github.b3kt.aviation.infrastructure.config.CacheConfiguration;

import lombok.RequiredArgsConstructor;
import net.iakovlev.timeshape.TimeZoneEngine;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Optional;

/**
 * A simple implementation of TimezoneResolver that approximates the timezone
 * based on the longitude. This is not accurate for political boundaries but
 * provides a valid timezone offset.
 */
@Service
@RequiredArgsConstructor
public class LongitudeTimezoneResolver implements TimezoneResolver {

    private final TimeZoneEngine timeZoneEngine;

    /**
     * Returns the timezone for the given coordinates.
     * 
     * @param latitude  latitude in seconds format
     * @param longitude longitude in seconds format
     * @return ZoneId representing the timezone
     */
    private ZoneId getZoneId(BigDecimal latitude, BigDecimal longitude) {
        Optional<ZoneId> zone = timeZoneEngine.query(
                latitude.doubleValue(),
                longitude.doubleValue());
        return zone.orElse(ZoneId.of("UTC")); // fallback if unknown
    }

    /**
     * Returns the timezone name for the given coordinates.
     * 
     * @param latitude  latitude in seconds format
     * @param longitude longitude in seconds format
     * @return String representing the timezone name
     */
    private String getZoneName(BigDecimal latitude, BigDecimal longitude) {
        return getZoneId(latitude, longitude).getId();
    }

    /**
     * Resolves the timezone ID for the given coordinates.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     * @return the timezone ID (e.g., "America/New_York", "UTC", "+05:00")
     */
    @Cacheable(value = CacheConfiguration.TIMEZONE_CACHE, key = "#latitude + #longitude")
    @Override
    public String resolve(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return "UTC";
        }
        return getZoneName(latitude, longitude);
    }
}
