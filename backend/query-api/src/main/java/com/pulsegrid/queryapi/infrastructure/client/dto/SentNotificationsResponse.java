package com.pulsegrid.queryapi.infrastructure.client.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SentNotificationsResponse(List<Notification> notifications) {

    public record Notification(UUID alertId, Instant notifiedAt) {
    }
}
