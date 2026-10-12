package com.pulsegrid.queryapi.application.service;

import com.pulsegrid.queryapi.application.error.InvalidQueryArgumentException;
import com.pulsegrid.queryapi.application.model.NotificationView;
import com.pulsegrid.queryapi.application.port.in.NotificationQueryUseCase;
import com.pulsegrid.queryapi.application.port.out.NotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class NotificationQueryService implements NotificationQueryUseCase {

    private static final int MAX_LIMIT = 200;

    private final NotificationPort notificationPort;

    @Override
    public Map<UUID, NotificationView> getNotificationsByAlertIds(Collection<UUID> alertIds) {
        if (alertIds.isEmpty()) {
            return Map.of();
        }
        return notificationPort.findByAlertIds(alertIds).stream()
                .collect(Collectors.toMap(NotificationView::alertId, Function.identity()));
    }

    @Override
    public List<NotificationView> getMostRecent(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new InvalidQueryArgumentException("'limit' debe estar entre 1 y " + MAX_LIMIT);
        }
        return notificationPort.findMostRecent(limit);
    }
}
