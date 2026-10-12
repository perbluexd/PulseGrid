package com.pulsegrid.notificationworkers.api.dto;

import java.time.Instant;
import java.util.UUID;

public record SentNotificationResponse(UUID alertId, Instant notifiedAt) {
}
