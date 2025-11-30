package com.github.b3kt.aviation.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.b3kt.aviation.infrastructure.config.properties.AviationApiProperties;

import net.iakovlev.timeshape.TimeZoneEngine;

/**
 * Main configuration class to enable configuration properties.
 */
@Configuration
@EnableConfigurationProperties(AviationApiProperties.class)
public class ApplicationConfiguration {

    @Configuration
public class TimeZoneEngineConfig {

    /**
     * Creates a single cached instance of TimeZoneEngine.
     * TimeZoneEngine loads ~1–2 MB geometry data, so this must be done once.
     */
    @Bean
    public TimeZoneEngine timeZoneEngine() {
        return TimeZoneEngine.initialize();
    }
}
}
