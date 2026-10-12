package com.pulsegrid.aggregationservice.infrastructure.persistence.adapter;

import com.pulsegrid.aggregationservice.application.port.out.TelemetryQueryPort;
import com.pulsegrid.aggregationservice.domain.model.BucketSize;
import com.pulsegrid.aggregationservice.domain.model.MetricBucket;
import com.pulsegrid.aggregationservice.domain.model.TelemetryReading;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class TelemetryQueryJdbcAdapter implements TelemetryQueryPort {

    private static final String LATEST_SQL = """
            SELECT DISTINCT ON (device_id, metric_type) device_id, metric_type, value, recorded_at
            FROM telemetry_reading
            WHERE device_id IN (:deviceIds)
            ORDER BY device_id, metric_type, recorded_at DESC
            """;

    private static final String HISTORY_SQL = """
            SELECT time_bucket(CAST(:interval AS INTERVAL), bucket)   AS bucket_start,
                   SUM(avg_value * sample_count) / SUM(sample_count) AS avg_value,
                   MIN(min_value)                                    AS min_value,
                   MAX(max_value)                                    AS max_value,
                   SUM(sample_count)                                 AS sample_count
            FROM metric_aggregate
            WHERE device_id = :deviceId
              AND metric_type = :metricType
              AND bucket >= :from
              AND bucket < :to
            GROUP BY bucket_start
            ORDER BY bucket_start
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<TelemetryReading> findLatestByDeviceIds(List<UUID> deviceIds) {
        var params = new MapSqlParameterSource("deviceIds", deviceIds);
        return namedParameterJdbcTemplate.query(LATEST_SQL, params, (rs, rowNum) -> new TelemetryReading(
                rs.getObject("device_id", UUID.class),
                rs.getString("metric_type"),
                rs.getDouble("value"),
                rs.getTimestamp("recorded_at").toInstant()));
    }

    @Override
    public List<MetricBucket> findHistory(UUID deviceId, String metricType, Instant from, Instant to, BucketSize bucketSize) {
        var params = new MapSqlParameterSource()
                .addValue("interval", bucketSize.interval())
                .addValue("deviceId", deviceId)
                .addValue("metricType", metricType)
                .addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to));
        return namedParameterJdbcTemplate.query(HISTORY_SQL, params, (rs, rowNum) -> new MetricBucket(
                rs.getTimestamp("bucket_start").toInstant(),
                rs.getDouble("avg_value"),
                rs.getDouble("min_value"),
                rs.getDouble("max_value"),
                rs.getLong("sample_count")));
    }
}
