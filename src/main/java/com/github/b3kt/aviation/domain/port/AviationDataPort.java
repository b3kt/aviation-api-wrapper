package com.github.b3kt.aviation.domain.port;

import com.github.b3kt.aviation.domain.model.Airport;
import reactor.core.publisher.Mono;

/**
 * Port (interface) for aviation data retrieval.
 * This interface defines the contract for fetching airport data,
 * keeping the domain layer independent of infrastructure details.
 * 
 * Implementations should handle:
 * - External API calls
 * - Caching
 * - Resilience patterns (retries, circuit breakers)
 */
public interface AviationDataPort {

    /**
     * Retrieves airport information by ICAO code.
     * 
     * @param icaoCode the 4-character ICAO code
     * @return Mono emitting the Airport or error if not found
     */
    Mono<Airport> getAirportByIcao(String icaoCode);
}
