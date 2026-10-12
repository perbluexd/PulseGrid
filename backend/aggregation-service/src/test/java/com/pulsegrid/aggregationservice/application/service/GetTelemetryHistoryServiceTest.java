package com.pulsegrid.aggregationservice.application.service;

import com.pulsegrid.aggregationservice.application.command.GetTelemetryHistoryCommand;
import com.pulsegrid.aggregationservice.application.error.InvalidTimeRangeException;
import com.pulsegrid.aggregationservice.application.port.out.TelemetryQueryPort;
import com.pulsegrid.aggregationservice.domain.model.BucketSize;
import com.pulsegrid.aggregationservice.domain.model.MetricBucket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTelemetryHistoryServiceTest {

    private static final UUID DEVICE_ID = UUID.randomUUID();
    private static final Instant FROM = Instant.parse("2026-09-20T10:00:00Z");
    private static final Instant TO = Instant.parse("2026-09-20T11:00:00Z");

    @Mock
    private TelemetryQueryPort telemetryQueryPort;

    @InjectMocks
    private GetTelemetryHistoryService getTelemetryHistoryService;

    @Test
    void shouldReturnBucketsFromQueryPort() {
        var bucket = new MetricBucket(FROM, 25.0, 20.0, 30.0, 2);
        when(telemetryQueryPort.findHistory(DEVICE_ID, "TEMPERATURE", FROM, TO, BucketSize.ONE_MINUTE))
                .thenReturn(List.of(bucket));

        var result = getTelemetryHistoryService.getHistory(
                new GetTelemetryHistoryCommand(DEVICE_ID, "TEMPERATURE", FROM, TO, BucketSize.ONE_MINUTE));

        assertThat(result).containsExactly(bucket);
    }

    @Test
    void shouldRejectRangeWhenFromIsNotBeforeTo() {
        var command = new GetTelemetryHistoryCommand(DEVICE_ID, "TEMPERATURE", TO, FROM, BucketSize.ONE_MINUTE);

        assertThatThrownBy(() -> getTelemetryHistoryService.getHistory(command))
                .isInstanceOf(InvalidTimeRangeException.class);
        verify(telemetryQueryPort, never()).findHistory(any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectRangeLongerThan31Days() {
        var command = new GetTelemetryHistoryCommand(DEVICE_ID, "TEMPERATURE", FROM,
                FROM.plusSeconds(32L * 24 * 3600), BucketSize.ONE_DAY);

        assertThatThrownBy(() -> getTelemetryHistoryService.getHistory(command))
                .isInstanceOf(InvalidTimeRangeException.class);
    }
}
