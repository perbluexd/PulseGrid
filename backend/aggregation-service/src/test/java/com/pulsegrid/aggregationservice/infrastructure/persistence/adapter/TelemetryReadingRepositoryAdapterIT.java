package com.pulsegrid.aggregationservice.infrastructure.persistence.adapter;

import com.pulsegrid.aggregationservice.domain.model.TelemetryReading;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(TelemetryReadingRepositoryAdapter.class)
@Testcontainers
class TelemetryReadingRepositoryAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> timescale = new PostgreSQLContainer<>(
            DockerImageName.parse("timescale/timescaledb:latest-pg16").asCompatibleSubstituteFor("postgres"));

    @Autowired
    private TelemetryReadingRepositoryAdapter telemetryReadingRepositoryAdapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSaveTelemetryReadingInHypertable() {
        UUID deviceId = UUID.randomUUID();

        telemetryReadingRepositoryAdapter.save(
                new TelemetryReading(deviceId, "TEMPERATURE", 23.5, Instant.parse("2026-09-20T10:15:30Z")));

        Double value = jdbcTemplate.queryForObject(
                "SELECT value FROM telemetry_reading WHERE device_id = ?", Double.class, deviceId);
        assertThat(value).isEqualTo(23.5);
    }

    @Test
    void shouldIgnoreDuplicateReadingWithSameNaturalKey() {
        UUID deviceId = UUID.randomUUID();
        Instant recordedAt = Instant.parse("2026-09-20T10:15:30Z");

        telemetryReadingRepositoryAdapter.save(new TelemetryReading(deviceId, "TEMPERATURE", 23.5, recordedAt));
        telemetryReadingRepositoryAdapter.save(new TelemetryReading(deviceId, "TEMPERATURE", 23.5, recordedAt));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM telemetry_reading WHERE device_id = ?", Integer.class, deviceId);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldAggregateReadingsByMinuteInContinuousAggregate() {
        UUID deviceId = UUID.randomUUID();

        telemetryReadingRepositoryAdapter.save(
                new TelemetryReading(deviceId, "TEMPERATURE", 20.0, Instant.parse("2026-09-20T10:15:10Z")));
        telemetryReadingRepositoryAdapter.save(
                new TelemetryReading(deviceId, "TEMPERATURE", 30.0, Instant.parse("2026-09-20T10:15:40Z")));
        jdbcTemplate.execute("CALL refresh_continuous_aggregate('metric_aggregate', NULL, NULL)");

        Map<String, Object> aggregate = jdbcTemplate.queryForMap(
                "SELECT avg_value, min_value, max_value, sample_count FROM metric_aggregate WHERE device_id = ?",
                deviceId);
        assertThat(aggregate.get("avg_value")).isEqualTo(25.0);
        assertThat(aggregate.get("min_value")).isEqualTo(20.0);
        assertThat(aggregate.get("max_value")).isEqualTo(30.0);
        assertThat(aggregate.get("sample_count")).isEqualTo(2L);
    }
}
