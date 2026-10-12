package com.pulsegrid.aggregationservice.api.controller;

import com.pulsegrid.aggregationservice.api.dto.LatestReadingResponse;
import com.pulsegrid.aggregationservice.api.dto.LatestReadingsResponse;
import com.pulsegrid.aggregationservice.api.dto.MetricBucketResponse;
import com.pulsegrid.aggregationservice.api.dto.TelemetryHistoryResponse;
import com.pulsegrid.aggregationservice.application.command.GetLatestReadingsCommand;
import com.pulsegrid.aggregationservice.application.command.GetTelemetryHistoryCommand;
import com.pulsegrid.aggregationservice.application.port.in.GetLatestReadingsUseCase;
import com.pulsegrid.aggregationservice.application.port.in.GetTelemetryHistoryUseCase;
import com.pulsegrid.aggregationservice.domain.model.BucketSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/internal/telemetry")
@RequiredArgsConstructor
public class InternalTelemetryController {

    private final GetLatestReadingsUseCase getLatestReadingsUseCase;
    private final GetTelemetryHistoryUseCase getTelemetryHistoryUseCase;

    @GetMapping("/latest")
    public ResponseEntity<LatestReadingsResponse> latest(@RequestParam @NotEmpty @Size(max = 200) List<UUID> deviceIds) {
        var readings = getLatestReadingsUseCase.getLatest(new GetLatestReadingsCommand(deviceIds)).stream()
                .map(r -> new LatestReadingResponse(r.deviceId(), r.metricType(), r.value(), r.recordedAt()))
                .toList();
        return ResponseEntity.ok(new LatestReadingsResponse(readings));
    }

    @GetMapping("/history")
    public ResponseEntity<TelemetryHistoryResponse> history(
            @RequestParam UUID deviceId,
            @RequestParam @NotBlank String metricType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "ONE_MINUTE") BucketSize bucketSize) {
        var command = new GetTelemetryHistoryCommand(deviceId, metricType, from, to, bucketSize);
        var buckets = getTelemetryHistoryUseCase.getHistory(command).stream()
                .map(b -> new MetricBucketResponse(b.bucketStart(), b.avgValue(), b.minValue(), b.maxValue(), b.sampleCount()))
                .toList();
        return ResponseEntity.ok(new TelemetryHistoryResponse(deviceId, metricType, bucketSize.name(), buckets));
    }
}
