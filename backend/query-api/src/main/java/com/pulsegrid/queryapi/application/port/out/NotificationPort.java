package com.pulsegrid.queryapi.application.port.out;

import com.pulsegrid.queryapi.application.model.NotificationView;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface NotificationPort {
    List<NotificationView> findByAlertIds(Collection<UUID> alertIds);

    List<NotificationView> findMostRecent(int limit);
}
