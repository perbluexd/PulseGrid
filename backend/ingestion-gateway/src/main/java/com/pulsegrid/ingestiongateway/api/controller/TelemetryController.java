package com.pulsegrid.ingestiongateway.api.controller;

import com.pulsegrid.ingestiongateway.api.dto.TelemetryReadingRequest;
import com.pulsegrid.ingestiongateway.application.command.IngestTelemetryReadingCommand;
import com.pulsegrid.ingestiongateway.application.port.in.IngestTelemetryReadingUseCase;
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
public class TelemetryController {

    private static final String API_KEY_HEADER = "X-Api-Key";

    private final IngestTelemetryReadingUseCase ingestTelemetryReadingUseCase;

    @PostMapping
    public Mono<ResponseEntity<Void>> ingest(@RequestHeader(API_KEY_HEADER) String apiKey,
                                              @Valid @RequestBody TelemetryReadingRequest request) {
        var command = new IngestTelemetryReadingCommand(apiKey, request.metricType(), request.value(), request.recordedAt());
        return ingestTelemetryReadingUseCase.ingest(command)
                .map(result -> ResponseEntity.accepted().build());
    }
}
