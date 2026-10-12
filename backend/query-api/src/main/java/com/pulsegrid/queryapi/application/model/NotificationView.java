package com.pulsegrid.queryapi.application.model;

import java.time.Instant;
import java.util.UUID;

public record NotificationView(UUID alertId, Instant notifiedAt) {
}
