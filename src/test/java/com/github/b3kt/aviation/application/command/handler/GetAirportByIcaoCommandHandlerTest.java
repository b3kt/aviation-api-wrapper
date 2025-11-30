package com.github.b3kt.aviation.application.command.handler;

import com.github.b3kt.aviation.application.command.GetAirportByIcaoCommand;
import com.github.b3kt.aviation.domain.exception.AirportNotFoundException;
import com.github.b3kt.aviation.domain.exception.InvalidIcaoCodeException;
import com.github.b3kt.aviation.domain.model.Airport;
import com.github.b3kt.aviation.domain.port.AviationDataPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for GetAirportByIcaoCommandHandler.
 */
@ExtendWith(MockitoExtension.class)
class GetAirportByIcaoCommandHandlerTest {

    @Mock
    private AviationDataPort aviationDataPort;

    private GetAirportByIcaoCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetAirportByIcaoCommandHandler(aviationDataPort);
    }

    @Test
    void shouldHandleCommand_whenAirportExists() {
        // Given
        String icao = "KJFK";
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

        when(aviationDataPort.getAirportByIcao(icao)).thenReturn(Mono.just(airport));

        GetAirportByIcaoCommand command = new GetAirportByIcaoCommand(icao);

        // When & Then
        StepVerifier.create(handler.handle(command))
                .expectNextMatches(response -> response.icaoCode().equals("KJFK") &&
                        response.name().equals("John F Kennedy International Airport") &&
                        response.coordinates() != null &&
                        response.coordinates().latitude().compareTo(new BigDecimal("40.6398")) == 0)
                .verifyComplete();
    }

    @Test
    void shouldPropagateError_whenAirportNotFound() {
        // Given
        String icao = "XXXX";
        when(aviationDataPort.getAirportByIcao(icao))
                .thenReturn(Mono.error(new AirportNotFoundException(icao)));

        GetAirportByIcaoCommand command = new GetAirportByIcaoCommand(icao);

        // When & Then
        StepVerifier.create(handler.handle(command))
                .expectError(AirportNotFoundException.class)
                .verify();
    }

    @Test
    void shouldReturnCommandType() {
        // When & Then
        assert handler.getCommandType().equals(GetAirportByIcaoCommand.class);
    }

    @Test
    void shouldThrowProviderException_icaoNull() {
        // Expect constructor validation to fail
        assertThrows(InvalidIcaoCodeException.class, () -> new GetAirportByIcaoCommand(null));
    }
}
