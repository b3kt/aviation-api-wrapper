package com.github.b3kt.aviation.presentation.controller;

import com.github.b3kt.aviation.application.command.GetAirportByIcaoCommand;
import com.github.b3kt.aviation.application.dto.AirportResponse;
import com.github.b3kt.aviation.application.executor.CommandExecutor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for airport-related endpoints.
 * Provides access to airport information via ICAO code lookup.
 */
@RestController
@RequestMapping("/api/v1/airports")
@Tag(name = "Airports", description = "Airport information API")
@RequiredArgsConstructor
@Slf4j
public class AirportController {

    private final CommandExecutor commandExecutor;

    @GetMapping("/{icao}")
    @Operation(summary = "Get airport by ICAO code", description = "Retrieves detailed airport information using the 4-character ICAO code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Airport found successfully", content = @Content(schema = @Schema(implementation = AirportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ICAO code format"),
            @ApiResponse(responseCode = "404", description = "Airport not found"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
            @ApiResponse(responseCode = "503", description = "Service unavailable (circuit breaker open)")
    })
    public Mono<ResponseEntity<AirportResponse>> getAirportByIcao(
            @Parameter(description = "4-character ICAO code (e.g., KJFK, EGLL, YSSY)", example = "KJFK") @PathVariable String icao) {
        log.info("Received request for airport with ICAO: {}", icao);

        GetAirportByIcaoCommand command = new GetAirportByIcaoCommand(icao);

        return commandExecutor.execute(command)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Successfully processed request for ICAO: {}", icao));
    }
}
