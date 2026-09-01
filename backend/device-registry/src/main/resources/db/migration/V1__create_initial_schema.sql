CREATE TABLE dashboard_users (
    id             UUID PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    role           VARCHAR(50) NOT NULL,
    is_active      BOOLEAN NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL
);

CREATE TABLE devices (
    id             UUID PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    type           VARCHAR(50) NOT NULL,
    status         VARCHAR(50) NOT NULL,
    location       VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    last_seen_at   TIMESTAMPTZ
);

CREATE TABLE api_keys (
    id             UUID PRIMARY KEY,
    device_id      UUID NOT NULL REFERENCES devices (id),
    key_hash       VARCHAR(255) NOT NULL UNIQUE,
    status         VARCHAR(50) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    expires_at     TIMESTAMPTZ,
    revoked_at     TIMESTAMPTZ
);

CREATE INDEX idx_api_keys_device_id ON api_keys (device_id);

CREATE TABLE device_groups (
    id             UUID PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    VARCHAR(255)
);

CREATE TABLE device_group_memberships (
    id             UUID PRIMARY KEY,
    device_id      UUID NOT NULL REFERENCES devices (id),
    group_id       UUID NOT NULL REFERENCES device_groups (id),
    created_at     TIMESTAMPTZ NOT NULL,
    UNIQUE (device_id, group_id)
);

CREATE INDEX idx_device_group_memberships_device_id ON device_group_memberships (device_id);
CREATE INDEX idx_device_group_memberships_group_id ON device_group_memberships (group_id);
