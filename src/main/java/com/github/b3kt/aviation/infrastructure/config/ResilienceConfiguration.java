package com.github.b3kt.aviation.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.github.b3kt.aviation.infrastructure.config.properties.AviationApiProperties;

import java.time.Duration;

/**
 * Configuration for Resilience4j patterns: circuit breaker, retry, rate
 * limiter, and time limiter.
 * Production-ready settings with comprehensive logging.
 */
@Configuration
public class ResilienceConfiguration {

        private static final Logger log = LoggerFactory.getLogger(ResilienceConfiguration.class);
        private static final String AVIATION_API = "aviationApi";

        @Bean
        public CircuitBreakerRegistry circuitBreakerRegistry() {
                CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                                .slidingWindowSize(10)
                                .minimumNumberOfCalls(5)
                                .failureRateThreshold(50.0f)
                                .slowCallRateThreshold(100.0f)
                                .slowCallDurationThreshold(Duration.ofSeconds(5))
                                .waitDurationInOpenState(Duration.ofSeconds(30))
                                .permittedNumberOfCallsInHalfOpenState(3)
                                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                                .recordExceptions(WebClientResponseException.InternalServerError.class,
                                                WebClientResponseException.ServiceUnavailable.class,
                                                WebClientResponseException.GatewayTimeout.class)
                                .ignoreExceptions(WebClientResponseException.NotFound.class,
                                                WebClientResponseException.BadRequest.class)
                                .build();

                CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

                // Add event listeners for observability
                registry.circuitBreaker(AVIATION_API).getEventPublisher()
                                .onStateTransition(event -> log.warn("Circuit breaker state changed: {}", event))
                                .onError(event -> log.error("Circuit breaker recorded error: {}",
                                                event.getThrowable().getMessage()))
                                .onSuccess(event -> log.debug("Circuit breaker recorded success"));

                return registry;
        }

        @Bean
        public RetryRegistry retryRegistry(AviationApiProperties properties) {
                RetryConfig config = RetryConfig.custom()
                                .maxAttempts(properties.maxRetries())
                                .intervalFunction(attempt -> {
                                        // Exponential backoff: 500ms, 1000ms, 2000ms
                                        return properties.retryDelayMillis() * (long) Math.pow(2, attempt - 1);
                                })
                                .retryExceptions(WebClientResponseException.InternalServerError.class,
                                                WebClientResponseException.ServiceUnavailable.class,
                                                WebClientResponseException.GatewayTimeout.class)
                                .ignoreExceptions(WebClientResponseException.NotFound.class,
                                                WebClientResponseException.BadRequest.class)
                                .build();

                RetryRegistry registry = RetryRegistry.of(config);

                // Add event listeners
                registry.retry(AVIATION_API).getEventPublisher()
                                .onRetry(event -> log.warn("Retry attempt {} for aviation API call",
                                                event.getNumberOfRetryAttempts()))
                                .onError(event -> log.error("Retry failed after {} attempts",
                                                event.getNumberOfRetryAttempts()));

                return registry;
        }

        @Bean
        public RateLimiterRegistry rateLimiterRegistry() {
                RateLimiterConfig config = RateLimiterConfig.custom()
                                .limitForPeriod(100) // 100 calls
                                .limitRefreshPeriod(Duration.ofMinutes(1)) // per minute
                                .timeoutDuration(Duration.ofSeconds(5)) // wait up to 5s for permission
                                .build();

                RateLimiterRegistry registry = RateLimiterRegistry.of(config);

                // Add event listeners
                registry.rateLimiter(AVIATION_API).getEventPublisher()
                                .onSuccess(event -> log.debug("Rate limiter allowed call"))
                                .onFailure(event -> log.warn("Rate limiter rejected call"));

                return registry;
        }

        @Bean
        public TimeLimiterRegistry timeLimiterRegistry(AviationApiProperties properties) {
                TimeLimiterConfig config = TimeLimiterConfig.custom()
                                .timeoutDuration(Duration.ofSeconds(properties.timeoutSeconds()))
                                .cancelRunningFuture(true)
                                .build();

                return TimeLimiterRegistry.of(config);
        }
}
