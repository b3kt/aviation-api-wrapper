package com.github.b3kt.aviation.application.command.handler;

import com.github.b3kt.aviation.application.command.CommandHandler;
import com.github.b3kt.aviation.application.command.GetAirportByIcaoCommand;
import com.github.b3kt.aviation.application.dto.AirportResponse;
import com.github.b3kt.aviation.domain.port.AviationDataPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Handler for GetAirportByIcaoCommand.
 * Orchestrates the use case of retrieving airport information.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetAirportByIcaoCommandHandler implements CommandHandler<GetAirportByIcaoCommand, AirportResponse> {

    private final AviationDataPort aviationDataPort;

    @Override
    public Mono<AirportResponse> handle(GetAirportByIcaoCommand command) {
        log.info("Handling GetAirportByIcaoCommand for ICAO: {}", command.icaoCode());

        return aviationDataPort.getAirportByIcao(command.icaoCode())
                .map(AirportResponse::fromDomain)
                .doOnSuccess(response -> log.info("Successfully retrieved airport: {}", response))
                .doOnError(error -> log.error("Error retrieving airport with ICAO {}: {}",
                        command.icaoCode(), error.getMessage()))
                .onErrorResume(Mono::error);
    }

    @Override
    public Class<GetAirportByIcaoCommand> getCommandType() {
        return GetAirportByIcaoCommand.class;
    }
}
