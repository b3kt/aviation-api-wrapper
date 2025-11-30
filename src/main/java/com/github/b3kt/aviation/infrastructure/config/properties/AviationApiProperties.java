package com.github.b3kt.aviation.infrastructure.config.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Configuration properties for Aviation API integration.
 * Binds to application.yml properties.
 */
@ConfigurationProperties(prefix = "aviation.api")
public record AviationApiProperties(
        @NotBlank String baseUrl,
        @Min(1) Integer timeoutSeconds,
        @Min(1) Integer maxRetries,
        @Min(1) Long retryDelayMillis,
        @Min(1) Integer cacheTtlMinutes,
        @NotNull Map<String, String> paths) {

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
        if (paths == null) {
            paths = new HashMap<>();
        }
    }
}
