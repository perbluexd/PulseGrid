package com.pulsegrid.ingestiongateway.api.controller;

import com.pulsegrid.ingestiongateway.api.dto.TelemetryReadingRequest;
import com.pulsegrid.ingestiongateway.application.command.IngestTelemetryReadingCommand;
import com.pulsegrid.ingestiongateway.application.error.DeviceNotActiveException;
import com.pulsegrid.ingestiongateway.application.error.InvalidApiKeyException;
import com.pulsegrid.ingestiongateway.application.port.in.IngestTelemetryReadingUseCase;
import com.pulsegrid.ingestiongateway.application.port.result.IngestTelemetryReadingResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = TelemetryController.class)
class TelemetryControllerTest {

    private static final Instant RECORDED_AT = Instant.parse("2026-09-20T10:15:30Z");

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private IngestTelemetryReadingUseCase ingestTelemetryReadingUseCase;

    @Test
    void shouldReturn202WhenTelemetryIsIngestedSuccessfully() {
        UUID deviceId = UUID.randomUUID();
        var request = new TelemetryReadingRequest("TEMPERATURE", 23.5, RECORDED_AT);
        var expectedCommand = new IngestTelemetryReadingCommand("valid-api-key", "TEMPERATURE", 23.5, RECORDED_AT);

        when(ingestTelemetryReadingUseCase.ingest(expectedCommand))
                .thenReturn(Mono.just(new IngestTelemetryReadingResult(deviceId)));

        webTestClient.post().uri("/api/v1/telemetry")
                .header("X-Api-Key", "valid-api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody().isEmpty();
    }

    @Test
    void shouldReturn400WhenMetricTypeIsBlank() {
        var request = new TelemetryReadingRequest("", 23.5, RECORDED_AT);

        webTestClient.post().uri("/api/v1/telemetry")
                .header("X-Api-Key", "valid-api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ERR-000");

        verify(ingestTelemetryReadingUseCase, never()).ingest(any());
    }

    @Test
    void shouldReturn401WhenApiKeyIsInvalid() {
        when(ingestTelemetryReadingUseCase.ingest(any()))
                .thenReturn(Mono.error(new InvalidApiKeyException()));

        var request = new TelemetryReadingRequest("TEMPERATURE", 23.5, RECORDED_AT);

        webTestClient.post().uri("/api/v1/telemetry")
                .header("X-Api-Key", "revoked-api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ERR-004");
    }

    @Test
    void shouldReturn403WhenDeviceIsNotActive() {
        when(ingestTelemetryReadingUseCase.ingest(any()))
                .thenReturn(Mono.error(new DeviceNotActiveException()));

        var request = new TelemetryReadingRequest("TEMPERATURE", 23.5, RECORDED_AT);

        webTestClient.post().uri("/api/v1/telemetry")
                .header("X-Api-Key", "valid-api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("ERR-005");
    }
}
