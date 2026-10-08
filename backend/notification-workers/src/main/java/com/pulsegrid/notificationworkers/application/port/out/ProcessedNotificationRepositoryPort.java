package com.pulsegrid.notificationworkers.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedNotificationRepositoryPort {
    boolean markAsProcessed(UUID alertId, Instant processedAt);
}
