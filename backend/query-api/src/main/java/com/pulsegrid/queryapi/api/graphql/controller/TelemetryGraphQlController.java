package com.pulsegrid.queryapi.api.graphql.controller;

import com.pulsegrid.queryapi.application.model.BucketSize;
import com.pulsegrid.queryapi.application.model.ReadingView;
import com.pulsegrid.queryapi.application.model.TelemetryBucketView;
import com.pulsegrid.queryapi.application.port.in.TelemetryQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TelemetryGraphQlController {

    private final TelemetryQueryUseCase telemetryQueryUseCase;

    @QueryMapping
    public ReadingView latestReading(@Argument UUID deviceId, @Argument String metricType) {
        return telemetryQueryUseCase.getLatestReading(deviceId, metricType).orElse(null);
    }

    @QueryMapping
    public List<TelemetryBucketView> telemetryHistory(@Argument UUID deviceId, @Argument String metricType,
                                                      @Argument Instant from, @Argument Instant to,
                                                      @Argument BucketSize bucketSize) {
        return telemetryQueryUseCase.getHistory(deviceId, metricType, from, to, bucketSize);
    }
}
