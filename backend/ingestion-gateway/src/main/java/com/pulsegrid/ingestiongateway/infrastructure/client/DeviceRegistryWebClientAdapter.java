package com.pulsegrid.ingestiongateway.infrastructure.client;

import com.pulsegrid.ingestiongateway.application.error.DeviceNotActiveException;
import com.pulsegrid.ingestiongateway.application.error.InvalidApiKeyException;
import com.pulsegrid.ingestiongateway.application.port.out.DeviceRegistryPort;
import com.pulsegrid.ingestiongateway.infrastructure.client.dto.ResolveByApiKeyRequest;
import com.pulsegrid.ingestiongateway.infrastructure.client.dto.ResolveByApiKeyResponse;
import com.pulsegrid.ingestiongateway.infrastructure.security.InternalServiceTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeviceRegistryWebClientAdapter implements DeviceRegistryPort {

    private static final String RESOLVE_BY_API_KEY_PATH = "/api/v1/internal/devices/resolve-by-key";
    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";

    private final WebClient deviceRegistryWebClient;
    private final InternalServiceTokenProvider internalServiceTokenProvider;

    @Override
    public Mono<UUID> resolveByApiKey(String apiKey) {
        return deviceRegistryWebClient.post()
                .uri(RESOLVE_BY_API_KEY_PATH)
                .header(INTERNAL_SERVICE_HEADER, internalServiceTokenProvider.getToken())
                .bodyValue(new ResolveByApiKeyRequest(apiKey))
                .retrieve()
                .onStatus(status -> status.equals(HttpStatus.UNAUTHORIZED),
                        response -> Mono.error(new InvalidApiKeyException()))
                .onStatus(status -> status.equals(HttpStatus.FORBIDDEN),
                        response -> Mono.error(new DeviceNotActiveException()))
                .bodyToMono(ResolveByApiKeyResponse.class)
                .map(ResolveByApiKeyResponse::deviceId);
    }
}
