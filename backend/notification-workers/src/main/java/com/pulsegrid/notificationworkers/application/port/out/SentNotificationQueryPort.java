package com.pulsegrid.notificationworkers.application.port.out;

import java.util.List;
import java.util.UUID;

public interface SentNotificationQueryPort {
    List<SentNotification> findByAlertIds(List<UUID> alertIds);

    List<SentNotification> findMostRecent(int limit);
}
