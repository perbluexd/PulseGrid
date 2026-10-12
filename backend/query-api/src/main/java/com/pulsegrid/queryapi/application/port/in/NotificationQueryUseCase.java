package com.pulsegrid.queryapi.application.port.in;

import com.pulsegrid.queryapi.application.model.NotificationView;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface NotificationQueryUseCase {
    Map<UUID, NotificationView> getNotificationsByAlertIds(Collection<UUID> alertIds);

    List<NotificationView> getMostRecent(int limit);
}
