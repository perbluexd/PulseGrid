package com.pulsegrid.queryapi.infrastructure.config;

import com.pulsegrid.queryapi.infrastructure.client.BearerTokenRelayInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ClientProperties.class)
public class RestClientConfig {

    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";

    @Bean
    public RestClient deviceRegistryRestClient(RestClient.Builder builder, ClientProperties properties) {
        return userFacing(builder, properties, properties.deviceRegistryBaseUrl());
    }

    @Bean
    public RestClient alertingRestClient(RestClient.Builder builder, ClientProperties properties) {
        return userFacing(builder, properties, properties.alertingServiceBaseUrl());
    }

    @Bean
    public RestClient aggregationRestClient(RestClient.Builder builder, ClientProperties properties,
                                            @Value("${app.internal.service-token}") String serviceToken) {
        return internal(builder, properties, properties.aggregationServiceBaseUrl(), serviceToken);
    }

    @Bean
    public RestClient notificationRestClient(RestClient.Builder builder, ClientProperties properties,
                                             @Value("${app.internal.service-token}") String serviceToken) {
        return internal(builder, properties, properties.notificationWorkersBaseUrl(), serviceToken);
    }

    private RestClient userFacing(RestClient.Builder builder, ClientProperties properties, String baseUrl) {
        return builder.clone()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory(properties))
                .requestInterceptor(new BearerTokenRelayInterceptor())
                .build();
    }

    private RestClient internal(RestClient.Builder builder, ClientProperties properties, String baseUrl,
                                String serviceToken) {
        return builder.clone()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory(properties))
                .defaultHeader(INTERNAL_SERVICE_HEADER, serviceToken)
                .build();
    }

    private SimpleClientHttpRequestFactory requestFactory(ClientProperties properties) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        return requestFactory;
    }
}
