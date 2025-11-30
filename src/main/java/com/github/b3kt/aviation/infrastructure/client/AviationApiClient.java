package com.github.b3kt.aviation.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.b3kt.aviation.domain.exception.AirportNotFoundException;
import com.github.b3kt.aviation.domain.model.Airport;
import com.github.b3kt.aviation.domain.port.AviationDataPort;
import com.github.b3kt.aviation.infrastructure.config.CacheConfiguration;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Implementation of AviationDataPort using WebClient to integrate with aviation
 * API.
 * Includes resilience patterns: circuit breaker, retry, rate limiter, and
 * caching.
 */
@Service
public class AviationApiClient implements AviationDataPort {

    private static final Logger log = LoggerFactory.getLogger(AviationApiClient.class);
    private static final String AVIATION_API = "aviationApi";

    private final WebClient webClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;

    public AviationApiClient(
            WebClient webClient,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            RateLimiterRegistry rateLimiterRegistry) {
        this.webClient = webClient;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    @Cacheable(value = CacheConfiguration.AIRPORT_CACHE, key = "#icaoCode")
    public Mono<Airport> getAirportByIcao(String icaoCode) {
        log.info("Fetching airport data for ICAO: {}", icaoCode);

        return webClient.get()
                .uri("/airports/{icao}", icaoCode)
                .retrieve()
                .bodyToMono(AviationApiResponse.class)
                .map(this::mapToDomain)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker(AVIATION_API)))
                .transformDeferred(RetryOperator.of(retryRegistry.retry(AVIATION_API)))
                .transformDeferred(RateLimiterOperator.of(rateLimiterRegistry.rateLimiter(AVIATION_API)))
                .doOnSuccess(airport -> log.info("Successfully fetched airport: {}", airport.name()))
                .doOnError(error -> log.error("Error fetching airport {}: {}", icaoCode, error.getMessage()))
                .onErrorResume(WebClientResponseException.class, this::handleWebClientException);
    }

    private Mono<Airport> handleWebClientException(WebClientResponseException ex) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return Mono.error(new AirportNotFoundException(extractIcaoFromError(ex)));
        }
        return Mono.error(ex);
    }

    private String extractIcaoFromError(WebClientResponseException ex) {
        // Extract ICAO from URI if possible
        String uri = ex.getRequest() != null ? ex.getRequest().getURI().toString() : "";
        String[] parts = uri.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "UNKNOWN";
    }

    private Airport mapToDomain(AviationApiResponse response) {
        return new Airport(
                response.icao(),
                response.iata(),
                response.name(),
                response.city(),
                response.country(),
                response.latitude() != null ? new BigDecimal(response.latitude()) : null,
                response.longitude() != null ? new BigDecimal(response.longitude()) : null,
                response.timezone(),
                response.elevation());
    }

    /**
     * DTO for aviation API response.
     * Ignores unknown properties for forward compatibility.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record AviationApiResponse(
            @JsonProperty("icao") String icao,
            @JsonProperty("iata") String iata,
            @JsonProperty("name") String name,
            @JsonProperty("city") String city,
            @JsonProperty("country") String country,
            @JsonProperty("lat") String latitude,
            @JsonProperty("lon") String longitude,
            @JsonProperty("timezone") String timezone,
            @JsonProperty("elevation") Integer elevation) {
    }
}
