package com.github.b3kt.aviation.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Aviation API integration.
 * Binds to application.yml properties.
 */
@ConfigurationProperties(prefix = "aviation.api")
public record AviationApiProperties(

        String baseUrl,

        Integer timeoutSeconds,

        Integer maxRetries,

        Long retryDelayMillis,

        Integer cacheTtlMinutes) {

    public AviationApiProperties {
        // Set defaults if not provided
        if (timeoutSeconds == null) {
            timeoutSeconds = 10;
        }
        if (maxRetries == null) {
            maxRetries = 3;
        }
        if (retryDelayMillis == null) {
            retryDelayMillis = 500L;
        }
        if (cacheTtlMinutes == null) {
            cacheTtlMinutes = 60;
        }
    }
}
