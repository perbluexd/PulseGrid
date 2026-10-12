package com.pulsegrid.queryapi.infrastructure.client;

import com.pulsegrid.queryapi.application.model.NotificationView;
import com.pulsegrid.queryapi.application.port.out.NotificationPort;
import com.pulsegrid.queryapi.infrastructure.client.dto.SentNotificationsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
public class NotificationRestClientAdapter implements NotificationPort {

    private static final String SERVICE = "notification-workers";

    private final RestClient restClient;

    public NotificationRestClientAdapter(@Qualifier("notificationRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<NotificationView> findByAlertIds(Collection<UUID> alertIds) {
        return toViews(DownstreamCall.execute(SERVICE, () -> restClient.get()
                .uri(uri -> uri.path("/api/v1/internal/notifications")
                        .queryParam("alertIds", alertIds.toArray())
                        .build())
                .retrieve()
                .body(SentNotificationsResponse.class)));
    }

    @Override
    public List<NotificationView> findMostRecent(int limit) {
        return toViews(DownstreamCall.execute(SERVICE, () -> restClient.get()
                .uri(uri -> uri.path("/api/v1/internal/notifications/recent")
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(SentNotificationsResponse.class)));
    }

    private List<NotificationView> toViews(SentNotificationsResponse response) {
        return response.notifications().stream()
                .map(n -> new NotificationView(n.alertId(), n.notifiedAt()))
                .toList();
    }
}
