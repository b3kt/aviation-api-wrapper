package com.github.b3kt.aviation.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine for high-performance in-memory caching.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    public static final String AIRPORT_CACHE = "airports";

    @Bean
    public CacheManager cacheManager(AviationApiProperties properties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(AIRPORT_CACHE);
        cacheManager.setCaffeine(caffeineCacheBuilder(properties));
        cacheManager.setAsyncCacheMode(true);
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder(AviationApiProperties properties) {
        return Caffeine.newBuilder()
                .expireAfterWrite(properties.cacheTtlMinutes(), TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats();
    }
}
