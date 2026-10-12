package com.pulsegrid.aggregationservice.application.service;

import com.pulsegrid.aggregationservice.application.command.GetLatestReadingsCommand;
import com.pulsegrid.aggregationservice.application.port.out.TelemetryQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetLatestReadingsServiceTest {

    @Mock
    private TelemetryQueryPort telemetryQueryPort;

    @InjectMocks
    private GetLatestReadingsService getLatestReadingsService;

    @Test
    void shouldNotQueryWhenNoDeviceIdsAreGiven() {
        var result = getLatestReadingsService.getLatest(new GetLatestReadingsCommand(List.of()));

        assertThat(result).isEmpty();
        verify(telemetryQueryPort, never()).findLatestByDeviceIds(any());
    }

    @Test
    void shouldQueryEachDeviceIdOnlyOnce() {
        UUID deviceId = UUID.randomUUID();

        getLatestReadingsService.getLatest(new GetLatestReadingsCommand(List.of(deviceId, deviceId)));

        verify(telemetryQueryPort).findLatestByDeviceIds(List.of(deviceId));
    }
}
