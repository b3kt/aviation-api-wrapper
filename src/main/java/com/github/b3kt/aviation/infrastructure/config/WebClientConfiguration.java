package com.github.b3kt.aviation.infrastructure.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for WebClient with optimized settings for production use.
 */
@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient webClient(WebClient.Builder builder, AviationApiProperties properties) {
        // Configure connection pooling
        ConnectionProvider connectionProvider = ConnectionProvider.builder("aviation-api-pool")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(45))
                .build();

        // Configure HTTP client with timeouts
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.timeoutSeconds() * 1000)
                .responseTimeout(Duration.ofSeconds(properties.timeoutSeconds()))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(properties.timeoutSeconds(), TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(properties.timeoutSeconds(), TimeUnit.SECONDS)));

        return builder
                .baseUrl(properties.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(this::configureCodecs)
                .build();
    }

    private void configureCodecs(ClientCodecConfigurer configurer) {
        // Increase buffer size for large responses
        configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024); // 2MB
    }
}
