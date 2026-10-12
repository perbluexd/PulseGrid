package com.pulsegrid.notificationworkers.application.service;

import com.pulsegrid.notificationworkers.application.port.in.GetSentNotificationsUseCase;
import com.pulsegrid.notificationworkers.application.port.out.SentNotification;
import com.pulsegrid.notificationworkers.application.port.out.SentNotificationQueryPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class GetSentNotificationsService implements GetSentNotificationsUseCase {

    private final SentNotificationQueryPort sentNotificationQueryPort;

    @Override
    public List<SentNotification> getByAlertIds(List<UUID> alertIds) {
        if (alertIds.isEmpty()) {
            return List.of();
        }
        return sentNotificationQueryPort.findByAlertIds(alertIds.stream().distinct().toList());
    }

    @Override
    public List<SentNotification> getMostRecent(int limit) {
        return sentNotificationQueryPort.findMostRecent(limit);
    }
}
