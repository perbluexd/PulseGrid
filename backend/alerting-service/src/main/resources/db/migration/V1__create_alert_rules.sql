CREATE TABLE alert_rules (
    id               UUID PRIMARY KEY,
    device_id        UUID,
    group_id         UUID,
    metric_type      VARCHAR(50) NOT NULL,
    alert_condition  VARCHAR(50) NOT NULL,
    threshold        DOUBLE PRECISION NOT NULL,
    severity         VARCHAR(50) NOT NULL,
    is_active        BOOLEAN NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_alert_rules_single_target CHECK (num_nonnulls(device_id, group_id) = 1)
);

CREATE INDEX idx_alert_rules_device_id ON alert_rules (device_id) WHERE device_id IS NOT NULL;
CREATE INDEX idx_alert_rules_group_id ON alert_rules (group_id) WHERE group_id IS NOT NULL;
