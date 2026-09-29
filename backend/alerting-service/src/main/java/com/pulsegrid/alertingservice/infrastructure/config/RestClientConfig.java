package com.pulsegrid.alertingservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";

    @Bean
    public RestClient deviceRegistryRestClient(RestClient.Builder builder,
                                               @Value("${app.device-registry.base-url}") String baseUrl,
                                               @Value("${app.internal.service-token}") String serviceToken) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        return builder
                .baseUrl(baseUrl)
                .defaultHeader(INTERNAL_SERVICE_HEADER, serviceToken)
                .requestFactory(requestFactory)
                .build();
    }
}
