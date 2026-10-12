package com.pulsegrid.queryapi.api.graphql.controller;

import com.pulsegrid.queryapi.application.model.AlertView;
import com.pulsegrid.queryapi.application.model.NotificationView;
import com.pulsegrid.queryapi.application.port.in.AlertQueryUseCase;
import com.pulsegrid.queryapi.application.port.in.NotificationQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NotificationGraphQlController {

    private final NotificationQueryUseCase notificationQueryUseCase;
    private final AlertQueryUseCase alertQueryUseCase;

    @QueryMapping
    public List<NotificationView> notificationHistory(@Argument int limit) {
        return notificationQueryUseCase.getMostRecent(limit);
    }

    @BatchMapping(typeName = "Notification", field = "alert")
    public Map<NotificationView, AlertView> alert(List<NotificationView> notifications) {
        var alerts = alertQueryUseCase.getAlertsByIds(notifications.stream().map(NotificationView::alertId).toList());
        Map<NotificationView, AlertView> result = new HashMap<>();
        notifications.forEach(n -> {
            var alert = alerts.get(n.alertId());
            if (alert != null) {
                result.put(n, alert);
            }
        });
        return result;
    }
}
