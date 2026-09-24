CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE telemetry_reading (
    device_id   UUID             NOT NULL,
    metric_type VARCHAR(50)      NOT NULL,
    value       DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMPTZ      NOT NULL,
    PRIMARY KEY (device_id, metric_type, recorded_at)
);

SELECT create_hypertable('telemetry_reading', 'recorded_at');
