package com.pulsegrid.queryapi.api.graphql.controller;

import com.pulsegrid.queryapi.application.model.BucketSize;
import com.pulsegrid.queryapi.application.model.DeviceGroupView;
import com.pulsegrid.queryapi.application.model.DeviceStatus;
import com.pulsegrid.queryapi.application.model.DeviceView;
import com.pulsegrid.queryapi.application.model.ReadingView;
import com.pulsegrid.queryapi.application.model.TelemetryBucketView;
import com.pulsegrid.queryapi.application.port.in.DeviceQueryUseCase;
import com.pulsegrid.queryapi.application.port.in.TelemetryQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DeviceGraphQlController {

    private final DeviceQueryUseCase deviceQueryUseCase;
    private final TelemetryQueryUseCase telemetryQueryUseCase;

    @QueryMapping
    public DeviceView device(@Argument UUID id) {
        return deviceQueryUseCase.getDevice(id).orElse(null);
    }

    @QueryMapping
    public List<DeviceView> devices(@Argument DeviceStatus status) {
        return deviceQueryUseCase.listDevices(status);
    }

    @QueryMapping
    public List<DeviceGroupView> deviceGroups() {
        return deviceQueryUseCase.listGroups();
    }

    @BatchMapping(typeName = "Device", field = "latestReadings")
    public Map<DeviceView, List<ReadingView>> latestReadings(List<DeviceView> devices) {
        var deviceIds = devices.stream().map(DeviceView::id).toList();
        var readingsByDevice = telemetryQueryUseCase.getLatestReadingsByDevice(deviceIds);
        return devices.stream().collect(Collectors.toMap(
                Function.identity(),
                device -> readingsByDevice.getOrDefault(device.id(), List.of()),
                (first, second) -> first));
    }

    @SchemaMapping(typeName = "Device", field = "telemetryHistory")
    public List<TelemetryBucketView> telemetryHistory(DeviceView device, @Argument String metricType,
                                                      @Argument Instant from, @Argument Instant to,
                                                      @Argument BucketSize bucketSize) {
        return telemetryQueryUseCase.getHistory(device.id(), metricType, from, to, bucketSize);
    }
}
