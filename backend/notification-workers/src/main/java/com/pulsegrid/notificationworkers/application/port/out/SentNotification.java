package com.pulsegrid.notificationworkers.application.port.out;

import java.time.Instant;
import java.util.UUID;

public record SentNotification(UUID alertId, Instant notifiedAt) {
}
