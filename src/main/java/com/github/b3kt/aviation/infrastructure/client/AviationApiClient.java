package com.github.b3kt.aviation.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.b3kt.aviation.application.helper.CoordinateHelper;
import com.github.b3kt.aviation.domain.exception.AirportNotFoundException;
import com.github.b3kt.aviation.domain.model.Airport;
import com.github.b3kt.aviation.domain.port.AviationDataPort;
import com.github.b3kt.aviation.domain.service.TimezoneResolver;
import com.github.b3kt.aviation.infrastructure.config.CacheConfiguration;
import com.github.b3kt.aviation.infrastructure.config.properties.AviationApiProperties;

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
import java.util.List;
import java.util.Map;

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
    private final AviationApiProperties aviationApiProperties;
    private final ObjectMapper objectMapper;
    private final TimezoneResolver timezoneResolver;

    public AviationApiClient(
            WebClient webClient,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            RateLimiterRegistry rateLimiterRegistry,
            AviationApiProperties aviationApiProperties,
            ObjectMapper objectMapper, TimezoneResolver timezoneResolver) {
        this.webClient = webClient;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.aviationApiProperties = aviationApiProperties;
        this.objectMapper = objectMapper;
        this.timezoneResolver = timezoneResolver;
    }

    @Override
    @Cacheable(value = CacheConfiguration.AIRPORT_CACHE, key = "#icaoCode")
    public Mono<Airport> getAirportByIcao(String icaoCode) {
        log.info("Fetching airport data for ICAO: {}", icaoCode);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(aviationApiProperties.paths().get("airports"))
                        .queryParam("apt", icaoCode)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> this.mapToDomain(icaoCode, response))
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

    private Airport mapToDomain(String icaoCode, String response) {
        try {
            TypeReference<Map<String, List<AirportRecord>>> typeRef = new TypeReference<>() {
            };
            Map<String, List<AirportRecord>> dynamicResult = objectMapper.readValue(response, typeRef);
            AirportRecord airportRecord = dynamicResult.get(icaoCode).getFirst();
            BigDecimal latitude = CoordinateHelper.parseFromSeconds(airportRecord.latitudeSec());
            BigDecimal longitude = CoordinateHelper.parseFromSeconds(airportRecord.longitudeSec());
            return new Airport(
                    airportRecord.icaoIdent(),
                    airportRecord.faaIdent(),
                    airportRecord.facilityName(),
                    airportRecord.city(),
                    airportRecord.country(),
                    latitude,
                    longitude,
                    timezoneResolver.resolve(latitude, longitude),
                    Integer.parseInt(airportRecord.elevation()));
        } catch (Exception e) {
            log.error("Error mapping response to domain: {}", e.getMessage());
            return new Airport("", "", "", "", "", null, null, null, null);
        }
    }

    /**
     * Record representing a single airport from the API response.
     * the attributes are refers to the API specification from @link
     * <a href="https://docs.aviationapi.com/#tag/airports">...</a>
     **/
    @JsonIgnoreProperties(ignoreUnknown = true)
    record AirportRecord(
            @JsonProperty("site_number") String siteNumber,
            @JsonProperty("type") String type,
            @JsonProperty("facility_name") String facilityName,
            @JsonProperty("faa_ident") String faaIdent,
            @JsonProperty("icao_ident") String icaoIdent,
            @JsonProperty("region") String region,
            @JsonProperty("district_office") String districtOffice,
            @JsonProperty("state") String state,
            @JsonProperty("state_full") String stateFull,
            @JsonProperty("county") String country,
            @JsonProperty("city") String city,
            @JsonProperty("ownership") String ownership,
            @JsonProperty("use") String use,
            @JsonProperty("manager") String manager,
            @JsonProperty("manager_phone") String managerPhone,
            @JsonProperty("latitude") String latitude,
            @JsonProperty("latitude_sec") String latitudeSec,
            @JsonProperty("longitude") String longitude,
            @JsonProperty("longitude_sec") String longitudeSec,
            @JsonProperty("elevation") String elevation,
            @JsonProperty("magnetic_variation") String magneticVariation,
            @JsonProperty("tpa") String tpa,
            @JsonProperty("vfr_sectional") String vfrSectional,
            @JsonProperty("boundary_artcc") String boundaryArtcc,
            @JsonProperty("boundary_artcc_name") String boundaryArtccName,
            @JsonProperty("responsible_artcc") String responsibleArtcc,
            @JsonProperty("responsible_artcc_name") String responsibleArtccName,
            @JsonProperty("fss_phone_number") String fssPhoneNumber,
            @JsonProperty("fss_phone_numer_tollfree") String fssPhoneNumberTollfree,
            @JsonProperty("notam_facility_ident") String notamFacilityIdent,
            @JsonProperty("status") String status,
            @JsonProperty("certification_typedate") String certificationTypedate,
            @JsonProperty("customs_airport_of_entry") String customsAirportOfEntry,
            @JsonProperty("military_joint_use") String militaryJointUse,
            @JsonProperty("military_landing") String militaryLanding,
            @JsonProperty("lighting_schedule") String lightingSchedule,
            @JsonProperty("beacon_schedule") String beaconSchedule,
            @JsonProperty("control_tower") String controlTower,
            @JsonProperty("unicom") String unicom,
            @JsonProperty("ctaf") String ctaf,
            @JsonProperty("effective_date") String effectiveDate) {
    }

    @JsonIgnoreProperties
    public record AirportResponse(
            Map<String, List<AirportRecord>> airports) {
    }
}
