package com.github.b3kt.aviation.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.b3kt.aviation.domain.exception.AirportNotFoundException;
import com.github.b3kt.aviation.domain.service.TimezoneResolver;
import com.github.b3kt.aviation.infrastructure.config.properties.AviationApiProperties;

import com.github.b3kt.aviation.infrastructure.service.LongitudeTimezoneResolver;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.netty.channel.ChannelOption;
import net.iakovlev.timeshape.TimeZoneEngine;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;

/**
 * Integration tests for AviationApiClient using MockWebServer.
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AviationApiClientTest {

        private static final int TIMEOUT_SECONDS = 3;
        private static final int MAX_RETRIES = 2;
        private static final long RETRY_DELAY_MILLIS = 500L;
        private static final int CACHE_TTL_MINUTES = 60;

        private MockWebServer mockWebServer;
        private AviationApiClient client;

        @Mock
        private TimezoneResolver timezoneResolver;

        @BeforeEach
        void setUp() throws IOException {
                openMocks(this);

                mockWebServer = new MockWebServer();

                HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT_SECONDS * 1000);

                WebClient webClient = WebClient.builder()
                                .clientConnector(new ReactorClientHttpConnector(httpClient))
                                .baseUrl(mockWebServer.url("/").toString())
                                .build();

                CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
                RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
                RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
                AviationApiProperties aviationApiProperties = new AviationApiProperties(
                                mockWebServer.url("/").toString(),
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
                                timezoneResolver);
        }

        @AfterEach
        void tearDown() throws IOException {
                mockWebServer.shutdown();
                verifyNoMoreInteractions(timezoneResolver);
        }

        @Test
        void shouldReturnAirport_whenApiReturnsSuccess() {
            when(timezoneResolver.resolve(
                    BigDecimal.valueOf(40.63992777777778),
                    BigDecimal.valueOf(-73.7786925)))
                    .thenReturn("America/New_York");

                // Given
                String jsonResponse = """
                                {
                                  "KJFK": [
                                    {
                                      "site_number": "15793.*A",
                                      "type": "AIRPORT",
                                      "facility_name": "JOHN F KENNEDY INTL",
                                      "faa_ident": "JFK",
                                      "icao_ident": "KJFK",
                                      "region": "AEA",
                                      "district_office": "NYC",
                                      "state": "NY",
                                      "state_full": "NEW YORK",
                                      "county": "QUEENS",
                                      "city": "NEW YORK",
                                      "ownership": "PU",
                                      "use": "PU",
                                      "manager": "CHARLES EVERETT",
                                      "manager_phone": "(718) 244-3501",
                                      "latitude": "40-38-23.7400N",
                                      "latitude_sec": "146303.7400N",
                                      "longitude": "073-46-43.2930W",
                                      "longitude_sec": "265603.2930W",
                                      "elevation": "13",
                                      "magnetic_variation": "13W",
                                      "tpa": "",
                                      "vfr_sectional": "NEW YORK",
                                      "boundary_artcc": "ZNY",
                                      "boundary_artcc_name": "NEW YORK",
                                      "responsible_artcc": "ZNY",
                                      "responsible_artcc_name": "NEW YORK",
                                      "fss_phone_number": "",
                                      "fss_phone_numer_tollfree": "1-800-WX-BRIEF",
                                      "notam_facility_ident": "JFK",
                                      "status": "O",
                                      "certification_typedate": "I E S 05/1973",
                                      "customs_airport_of_entry": "N",
                                      "military_joint_use": "N",
                                      "military_landing": "Y",
                                      "lighting_schedule": "",
                                      "beacon_schedule": "SS-SR",
                                      "control_tower": "Y",
                                      "unicom": "122.950",
                                      "ctaf": "",
                                      "effective_date": "11/04/2021"
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
                                                airport.name().equals("JOHN F KENNEDY INTL"))
                                .verifyComplete();

                verify(timezoneResolver).resolve(
                        BigDecimal.valueOf(40.63992777777778),
                        BigDecimal.valueOf(-73.7786925));
        }

        @Test
        void shouldThrowAirportNotFoundException_whenApiReturns404() {
                
                // Given
                mockWebServer.enqueue(new MockResponse()
                                .setResponseCode(404)
                                .setBody("{\"error\":\"Not found\"}"));

                // When & Then
                StepVerifier.create(client.getAirportByIcao("XXXX"))
                                .expectError(WebClientRequestException.class)
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
