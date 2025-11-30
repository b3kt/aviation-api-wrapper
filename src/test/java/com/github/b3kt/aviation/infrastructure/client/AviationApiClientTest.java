package com.github.b3kt.aviation.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.b3kt.aviation.domain.exception.AirportNotFoundException;
import com.github.b3kt.aviation.domain.model.Airport;
import com.github.b3kt.aviation.domain.service.TimezoneResolver;
import com.github.b3kt.aviation.infrastructure.config.properties.AviationApiProperties;

import com.github.b3kt.aviation.infrastructure.service.LongitudeTimezoneResolver;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration tests for AviationApiClient using MockWebServer.
 */
class AviationApiClientTest {

        private static final String BASE_URL = "http://localhost:8088";
        private static final int TIMEOUT_SECONDS = 10;
        private static final int MAX_RETRIES = 3;
        private static final long RETRY_DELAY_MILLIS = 500L;
        private static final int CACHE_TTL_MINUTES = 60;

        private MockWebServer mockWebServer;
        private AviationApiClient client;

        @BeforeEach
        void setUp() throws IOException {
                mockWebServer = new MockWebServer();
                mockWebServer.start(8088);

                WebClient webClient = WebClient.builder()
                                .baseUrl(mockWebServer.url("/").toString())
                                .build();

                CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
                RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
                RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
                TimezoneResolver timezoneResolver = new LongitudeTimezoneResolver();
                AviationApiProperties aviationApiProperties = new AviationApiProperties(
                                BASE_URL,
                                TIMEOUT_SECONDS,
                                MAX_RETRIES,
                                RETRY_DELAY_MILLIS,
                                CACHE_TTL_MINUTES,
                                new HashMap<>() {
                                        {
                                                put("airports", "/v1/airports");
                                        }
                                });

                client = new AviationApiClient(
                                webClient,
                                circuitBreakerRegistry,
                                retryRegistry,
                                rateLimiterRegistry,
                                aviationApiProperties,
                                new ObjectMapper(),
                                timezoneResolver
                        );
        }

        @AfterEach
        void tearDown() throws IOException {
                mockWebServer.shutdown();
        }

        @Test
        void shouldReturnAirport_whenApiReturnsSuccess() {
                // Given
                String jsonResponse = """
                                {
                                    "KJFK": [
                                        {
                                            "icao_ident": "KJFK",
                                            "iata": "JFK",
                                            "name": "John F Kennedy International Airport",
                                            "city": "New York",
                                            "country": "United States",
                                            "latitude": "40.6398",
                                            "longitude": "-73.7789",
                                            "timezone": "America/New_York",
                                            "elevation": 13
                                        }
                                    ]
                                }
                                """;

                mockWebServer.enqueue(new MockResponse()
                                .setBody(jsonResponse)
                                .addHeader("Content-Type", "application/json"));

                // When & Then
                StepVerifier.create(client.getAirportByIcao("KJFK"))
                                .expectNextMatches(airport -> airport.icaoCode().equals("KJFK") &&
                                                airport.name().equals("John F Kennedy International Airport"))
                                .verifyComplete();
        }

        @Test
        void shouldThrowAirportNotFoundException_whenApiReturns404() {
                // Given
                mockWebServer.enqueue(new MockResponse()
                                .setResponseCode(404)
                                .setBody("{\"error\":\"Not found\"}"));

                // When & Then
                StepVerifier.create(client.getAirportByIcao("XXXX"))
                                .expectError(AirportNotFoundException.class)
                                .verify();
        }

        @Test
        void shouldHandleTimeout() {
                // Given
                mockWebServer.enqueue(new MockResponse()
                                .setBody("{}")
                                .setBodyDelay(15, java.util.concurrent.TimeUnit.SECONDS));

                // When & Then
                StepVerifier.create(client.getAirportByIcao("KJFK"))
                                .expectError()
                                .verify();
        }
}
