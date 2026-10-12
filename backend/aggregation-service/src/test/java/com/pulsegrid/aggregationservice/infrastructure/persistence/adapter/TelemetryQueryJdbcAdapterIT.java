package com.pulsegrid.aggregationservice.infrastructure.persistence.adapter;

import com.pulsegrid.aggregationservice.domain.model.BucketSize;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({TelemetryReadingRepositoryAdapter.class, TelemetryQueryJdbcAdapter.class})
@Testcontainers
class TelemetryQueryJdbcAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> timescale = new PostgreSQLContainer<>(
            DockerImageName.parse("timescale/timescaledb:latest-pg16").asCompatibleSubstituteFor("postgres"));

    @Autowired
    private TelemetryReadingRepositoryAdapter telemetryReadingRepositoryAdapter;

    @Autowired
    private TelemetryQueryJdbcAdapter telemetryQueryJdbcAdapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldReturnOnlyTheMostRecentReadingPerDeviceAndMetric() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        save(first, "TEMPERATURE", 20.0, "2026-09-20T10:00:00Z");
        save(first, "TEMPERATURE", 22.0, "2026-09-20T10:05:00Z");
        save(first, "HUMIDITY", 55.0, "2026-09-20T10:01:00Z");
        save(second, "TEMPERATURE", 30.0, "2026-09-20T09:00:00Z");

        var latest = telemetryQueryJdbcAdapter.findLatestByDeviceIds(List.of(first, second));

        assertThat(latest).containsExactlyInAnyOrder(
                new TelemetryReading(first, "TEMPERATURE", 22.0, Instant.parse("2026-09-20T10:05:00Z")),
                new TelemetryReading(first, "HUMIDITY", 55.0, Instant.parse("2026-09-20T10:01:00Z")),
                new TelemetryReading(second, "TEMPERATURE", 30.0, Instant.parse("2026-09-20T09:00:00Z")));
    }

    @Test
    void shouldRebucketMinuteAggregatesWithWeightedAverage() {
        UUID deviceId = UUID.randomUUID();
        save(deviceId, "TEMPERATURE", 10.0, "2026-09-20T10:00:10Z");
        save(deviceId, "TEMPERATURE", 20.0, "2026-09-20T10:00:40Z");
        save(deviceId, "TEMPERATURE", 40.0, "2026-09-20T10:03:00Z");
        save(deviceId, "TEMPERATURE", 99.0, "2026-09-20T10:07:00Z");
        jdbcTemplate.execute("CALL refresh_continuous_aggregate('metric_aggregate', NULL, NULL)");

        var buckets = telemetryQueryJdbcAdapter.findHistory(deviceId, "TEMPERATURE",
                Instant.parse("2026-09-20T10:00:00Z"), Instant.parse("2026-09-20T10:10:00Z"), BucketSize.FIVE_MINUTES);

        assertThat(buckets).hasSize(2);
        assertThat(buckets.get(0).bucketStart()).isEqualTo(Instant.parse("2026-09-20T10:00:00Z"));
        assertThat(buckets.get(0).avgValue()).isCloseTo(70.0 / 3, offset(0.0001));
        assertThat(buckets.get(0).minValue()).isEqualTo(10.0);
        assertThat(buckets.get(0).maxValue()).isEqualTo(40.0);
        assertThat(buckets.get(0).sampleCount()).isEqualTo(3);
        assertThat(buckets.get(1).avgValue()).isEqualTo(99.0);
    }

    @Test
    void shouldExcludeBucketsOutsideTheRequestedRange() {
        UUID deviceId = UUID.randomUUID();
        save(deviceId, "TEMPERATURE", 10.0, "2026-09-20T09:59:00Z");
        save(deviceId, "TEMPERATURE", 20.0, "2026-09-20T10:01:00Z");
        jdbcTemplate.execute("CALL refresh_continuous_aggregate('metric_aggregate', NULL, NULL)");

        var buckets = telemetryQueryJdbcAdapter.findHistory(deviceId, "TEMPERATURE",
                Instant.parse("2026-09-20T10:00:00Z"), Instant.parse("2026-09-20T11:00:00Z"), BucketSize.ONE_MINUTE);

        assertThat(buckets).singleElement().satisfies(b -> assertThat(b.avgValue()).isEqualTo(20.0));
    }

    private void save(UUID deviceId, String metricType, double value, String recordedAt) {
        telemetryReadingRepositoryAdapter.save(new TelemetryReading(deviceId, metricType, value, Instant.parse(recordedAt)));
    }
}
