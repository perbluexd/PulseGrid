CREATE TABLE alerts (
    id               UUID PRIMARY KEY,
    alert_rule_id    UUID NOT NULL REFERENCES alert_rules (id),
    device_id        UUID NOT NULL,
    triggered_value  DOUBLE PRECISION NOT NULL,
    severity         VARCHAR(50) NOT NULL,
    status           VARCHAR(50) NOT NULL,
    triggered_at     TIMESTAMPTZ NOT NULL,
    resolved_at      TIMESTAMPTZ,
    acknowledged_by  UUID,
    resolved_by      UUID
);

CREATE UNIQUE INDEX uq_alerts_open_per_rule_and_device
    ON alerts (alert_rule_id, device_id)
    WHERE status IN ('TRIGGERED', 'ACKNOWLEDGED');

CREATE INDEX idx_alerts_triggered_at ON alerts (triggered_at DESC);
CREATE INDEX idx_alerts_device_id ON alerts (device_id);
