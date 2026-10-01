CREATE TABLE processed_notifications (
    alert_id      UUID PRIMARY KEY,
    processed_at  TIMESTAMPTZ NOT NULL
);
