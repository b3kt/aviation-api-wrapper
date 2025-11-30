package com.github.b3kt.aviation.application.command.handler;

import com.github.b3kt.aviation.application.command.CommandHandler;
import com.github.b3kt.aviation.application.command.GetAirportByIcaoCommand;
import com.github.b3kt.aviation.application.dto.AirportResponse;
import com.github.b3kt.aviation.domain.port.AviationDataPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Handler for GetAirportByIcaoCommand.
 * Orchestrates the use case of retrieving airport information.
 */
@Component
public class GetAirportByIcaoCommandHandler implements CommandHandler<GetAirportByIcaoCommand, AirportResponse> {

    private static final Logger log = LoggerFactory.getLogger(GetAirportByIcaoCommandHandler.class);

    private final AviationDataPort aviationDataPort;

    public GetAirportByIcaoCommandHandler(AviationDataPort aviationDataPort) {
        this.aviationDataPort = aviationDataPort;
    }

    @Override
    public Mono<AirportResponse> handle(GetAirportByIcaoCommand command) {
        log.info("Handling GetAirportByIcaoCommand for ICAO: {}", command.icaoCode());

        return aviationDataPort.getAirportByIcao(command.icaoCode())
                .map(AirportResponse::fromDomain)
                .doOnSuccess(response -> log.info("Successfully retrieved airport: {}", response.name()))
                .doOnError(error -> log.error("Error retrieving airport with ICAO {}: {}",
                        command.icaoCode(), error.getMessage()));
    }

    @Override
    public Class<GetAirportByIcaoCommand> getCommandType() {
        return GetAirportByIcaoCommand.class;
    }
}
