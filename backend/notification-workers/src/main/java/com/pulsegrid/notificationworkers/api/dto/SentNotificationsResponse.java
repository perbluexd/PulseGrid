package com.pulsegrid.notificationworkers.api.dto;

import java.util.List;

public record SentNotificationsResponse(List<SentNotificationResponse> notifications) {
}
