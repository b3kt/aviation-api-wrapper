package com.github.b3kt.aviation.presentation.controller;

import com.github.b3kt.aviation.application.dto.AirportResponse;
import com.github.b3kt.aviation.domain.exception.AirportNotFoundException;
import com.github.b3kt.aviation.domain.model.Airport;
import com.github.b3kt.aviation.domain.port.AviationDataPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

/**
 * Integration tests for AirportController.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AirportControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AviationDataPort aviationDataPort;

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(aviationDataPort);
    }

    @Test
    void shouldReturnAirport_whenValidIcaoProvided() {
        // Given
        Airport airport = new Airport(
                "KJFK",
                "JFK",
                "John F Kennedy International Airport",
                "New York",
                "United States",
                new BigDecimal("40.6398"),
                new BigDecimal("-73.7789"),
                "America/New_York",
                13);

        when(aviationDataPort.getAirportByIcao("KJFK")).thenReturn(Mono.just(airport));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/airports/KJFK")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AirportResponse.class)
                .value(response -> {
                    assert response != null;
                    assert response.icaoCode().equals("KJFK");
                    assert response.name().equals("John F Kennedy International Airport");
                });

        verify(aviationDataPort).getAirportByIcao("KJFK");
    }

    @Test
    void shouldReturn404_whenAirportNotFound() {
        // Given
        when(aviationDataPort.getAirportByIcao("XXXX"))
                .thenReturn(Mono.error(new AirportNotFoundException("XXXX")));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/airports/XXXX")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Airport Not Found")
                .jsonPath("$.message").exists();

        verify(aviationDataPort).getAirportByIcao("XXXX");
    }

    @Test
    void shouldReturn400_whenInvalidIcaoProvided() {
        // When & Then
        webTestClient.get()
                .uri("/api/v1/airports/INVALID123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldNormalizeIcaoToUppercase() {
        // Given
        Airport airport = new Airport(
                "KJFK",
                "JFK",
                "John F Kennedy International Airport",
                "New York",
                "United States",
                new BigDecimal("40.6398"),
                new BigDecimal("-73.7789"),
                "America/New_York",
                13);

        when(aviationDataPort.getAirportByIcao("KJFK")).thenReturn(Mono.just(airport));

        // When & Then
        webTestClient.get()
                .uri("/api/v1/airports/kjfk") // lowercase
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        verify(aviationDataPort).getAirportByIcao("KJFK");
    }
}
