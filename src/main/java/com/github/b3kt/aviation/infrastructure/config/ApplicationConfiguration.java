package com.github.b3kt.aviation.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Main configuration class to enable configuration properties.
 */
@Configuration
@EnableConfigurationProperties(AviationApiProperties.class)
public class ApplicationConfiguration {
}
