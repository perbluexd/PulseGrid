package com.pulsegrid.ingestiongateway.api.controller;

import com.pulsegrid.ingestiongateway.api.dto.TelemetryReadingRequest;
import com.pulsegrid.ingestiongateway.api.error.ErrorResponse;
import com.pulsegrid.ingestiongateway.application.command.IngestTelemetryReadingCommand;
import com.pulsegrid.ingestiongateway.application.port.in.IngestTelemetryReadingUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
@Tag(name = "Telemetry", description = "Ingesta de lecturas de telemetría enviadas por dispositivos")
public class TelemetryController {

    private static final String API_KEY_HEADER = "X-Api-Key";

    private final IngestTelemetryReadingUseCase ingestTelemetryReadingUseCase;

    @PostMapping
    @SecurityRequirement(name = "apiKeyAuth")
    @Operation(summary = "Ingerir una lectura de telemetría", description = "Resuelve la API key a un deviceId y publica el evento en telemetry-events")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Evento aceptado y publicado en Kafka"),
            @ApiResponse(responseCode = "400", description = "Request con campos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "API key inválida o revocada (ERR-004)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El dispositivo no está activo (ERR-005)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<ResponseEntity<Void>> ingest(@RequestHeader(API_KEY_HEADER) String apiKey,
                                              @Valid @RequestBody TelemetryReadingRequest request) {
        var command = new IngestTelemetryReadingCommand(apiKey, request.metricType(), request.value(), request.recordedAt());
        return ingestTelemetryReadingUseCase.ingest(command)
                .map(result -> ResponseEntity.accepted().build());
    }
}
