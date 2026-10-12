package com.pulsegrid.aggregationservice.api.controller;

import com.pulsegrid.aggregationservice.application.error.InvalidTimeRangeException;
import com.pulsegrid.aggregationservice.application.port.in.GetLatestReadingsUseCase;
import com.pulsegrid.aggregationservice.application.port.in.GetTelemetryHistoryUseCase;
import com.pulsegrid.aggregationservice.domain.model.MetricBucket;
import com.pulsegrid.aggregationservice.domain.model.TelemetryReading;
import com.pulsegrid.aggregationservice.infrastructure.config.SecurityConfig;
import com.pulsegrid.aggregationservice.infrastructure.security.InternalServiceAuthenticationFilter;
import com.pulsegrid.aggregationservice.infrastructure.security.InternalServiceTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalTelemetryController.class)
@Import({SecurityConfig.class, InternalServiceAuthenticationFilter.class, InternalServiceTokenProvider.class})
@TestPropertySource(properties = "app.internal.service-token=test-token")
class InternalTelemetryControllerTest {

    private static final String INTERNAL_HEADER = "X-Internal-Service";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetLatestReadingsUseCase getLatestReadingsUseCase;

    @MockitoBean
    private GetTelemetryHistoryUseCase getTelemetryHistoryUseCase;

    @Test
    void shouldReturn401WithoutInternalServiceHeader() throws Exception {
        mockMvc.perform(get("/api/v1/internal/telemetry/latest").param("deviceIds", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WithWrongInternalServiceToken() throws Exception {
        mockMvc.perform(get("/api/v1/internal/telemetry/latest")
                        .param("deviceIds", UUID.randomUUID().toString())
                        .header(INTERNAL_HEADER, "wrong-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnLatestReadingsForManyDevicesInOneCall() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        Instant recordedAt = Instant.parse("2026-09-20T10:15:30Z");
        when(getLatestReadingsUseCase.getLatest(any())).thenReturn(List.of(
                new TelemetryReading(first, "TEMPERATURE", 23.5, recordedAt),
                new TelemetryReading(second, "HUMIDITY", 60.0, recordedAt)));

        mockMvc.perform(get("/api/v1/internal/telemetry/latest")
                        .param("deviceIds", first + "," + second)
                        .header(INTERNAL_HEADER, "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readings.length()").value(2))
                .andExpect(jsonPath("$.readings[0].deviceId").value(first.toString()))
                .andExpect(jsonPath("$.readings[1].metricType").value("HUMIDITY"));
    }

    @Test
    void shouldReturnHistoryBuckets() throws Exception {
        UUID deviceId = UUID.randomUUID();
        when(getTelemetryHistoryUseCase.getHistory(any())).thenReturn(List.of(
                new MetricBucket(Instant.parse("2026-09-20T10:00:00Z"), 25.0, 20.0, 30.0, 2)));

        mockMvc.perform(get("/api/v1/internal/telemetry/history")
                        .param("deviceId", deviceId.toString())
                        .param("metricType", "TEMPERATURE")
                        .param("from", "2026-09-20T10:00:00Z")
                        .param("to", "2026-09-20T11:00:00Z")
                        .param("bucketSize", "FIVE_MINUTES")
                        .header(INTERNAL_HEADER, "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bucketSize").value("FIVE_MINUTES"))
                .andExpect(jsonPath("$.buckets[0].avgValue").value(25.0))
                .andExpect(jsonPath("$.buckets[0].sampleCount").value(2));
    }

    @Test
    void shouldReturn400WhenTimeRangeIsInvalid() throws Exception {
        when(getTelemetryHistoryUseCase.getHistory(any())).thenThrow(new InvalidTimeRangeException("rango inválido"));

        mockMvc.perform(get("/api/v1/internal/telemetry/history")
                        .param("deviceId", UUID.randomUUID().toString())
                        .param("metricType", "TEMPERATURE")
                        .param("from", "2026-09-20T11:00:00Z")
                        .param("to", "2026-09-20T10:00:00Z")
                        .header(INTERNAL_HEADER, "test-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("rango inválido"));
    }

    @Test
    void shouldReturn400WhenBucketSizeIsUnknown() throws Exception {
        mockMvc.perform(get("/api/v1/internal/telemetry/history")
                        .param("deviceId", UUID.randomUUID().toString())
                        .param("metricType", "TEMPERATURE")
                        .param("from", "2026-09-20T10:00:00Z")
                        .param("to", "2026-09-20T11:00:00Z")
                        .param("bucketSize", "ONE_WEEK")
                        .header(INTERNAL_HEADER, "test-token"))
                .andExpect(status().isBadRequest());
    }
}
