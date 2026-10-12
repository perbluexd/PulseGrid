package com.pulsegrid.queryapi.application.model;

import java.time.Instant;
import java.util.UUID;

public record DeviceView(UUID id, String name, String type, DeviceStatus status, String location,
                         Instant createdAt, Instant lastSeenAt) {
}
