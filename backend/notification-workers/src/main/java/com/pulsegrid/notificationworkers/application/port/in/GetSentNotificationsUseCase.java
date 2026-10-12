package com.pulsegrid.notificationworkers.application.port.in;

import com.pulsegrid.notificationworkers.application.port.out.SentNotification;

import java.util.List;
import java.util.UUID;

public interface GetSentNotificationsUseCase {
    List<SentNotification> getByAlertIds(List<UUID> alertIds);

    List<SentNotification> getMostRecent(int limit);
}
