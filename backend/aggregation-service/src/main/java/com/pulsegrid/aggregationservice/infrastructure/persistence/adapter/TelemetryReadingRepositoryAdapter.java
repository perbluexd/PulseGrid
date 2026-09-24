package com.pulsegrid.aggregationservice.infrastructure.persistence.adapter;

import com.pulsegrid.aggregationservice.application.port.out.TelemetryReadingRepositoryPort;
import com.pulsegrid.aggregationservice.domain.model.TelemetryReading;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@AllArgsConstructor
@Component
public class TelemetryReadingRepositoryAdapter implements TelemetryReadingRepositoryPort {

    private static final String INSERT_SQL = """
            INSERT INTO telemetry_reading (device_id, metric_type, value, recorded_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT DO NOTHING
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(TelemetryReading telemetryReading) {
        jdbcTemplate.update(INSERT_SQL,
                telemetryReading.deviceId(),
                telemetryReading.metricType(),
                telemetryReading.value(),
                Timestamp.from(telemetryReading.recordedAt()));
    }
}
