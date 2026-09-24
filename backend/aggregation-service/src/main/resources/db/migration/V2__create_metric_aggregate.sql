CREATE MATERIALIZED VIEW metric_aggregate
WITH (timescaledb.continuous) AS
SELECT device_id,
       metric_type,
       time_bucket(INTERVAL '1 minute', recorded_at) AS bucket,
       AVG(value)                                     AS avg_value,
       MIN(value)                                     AS min_value,
       MAX(value)                                     AS max_value,
       COUNT(*)                                       AS sample_count
FROM telemetry_reading
GROUP BY device_id, metric_type, bucket
WITH NO DATA;

SELECT add_continuous_aggregate_policy('metric_aggregate',
    start_offset      => INTERVAL '1 hour',
    end_offset        => INTERVAL '1 minute',
    schedule_interval => INTERVAL '1 minute');
