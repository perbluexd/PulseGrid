package com.pulsegrid.queryapi.api.graphql.controller;

import com.pulsegrid.queryapi.api.graphql.pagination.AlertConnection;
import com.pulsegrid.queryapi.api.graphql.pagination.AlertEdge;
import com.pulsegrid.queryapi.api.graphql.pagination.OffsetCursor;
import com.pulsegrid.queryapi.api.graphql.pagination.PageInfo;
import com.pulsegrid.queryapi.application.model.AlertFilter;
import com.pulsegrid.queryapi.application.model.AlertRuleView;
import com.pulsegrid.queryapi.application.model.AlertView;
import com.pulsegrid.queryapi.application.model.DeviceGroupView;
import com.pulsegrid.queryapi.application.model.DeviceView;
import com.pulsegrid.queryapi.application.model.NotificationView;
import com.pulsegrid.queryapi.application.port.in.AlertQueryUseCase;
import com.pulsegrid.queryapi.application.port.in.DeviceQueryUseCase;
import com.pulsegrid.queryapi.application.port.in.NotificationQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoader;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class AlertGraphQlController {

    private final AlertQueryUseCase alertQueryUseCase;
    private final DeviceQueryUseCase deviceQueryUseCase;
    private final NotificationQueryUseCase notificationQueryUseCase;

    @QueryMapping
    public AlertConnection alerts(@Argument AlertFilter filter, @Argument int first, @Argument String after) {
        int offset = after == null ? 0 : OffsetCursor.decode(after) + 1;
        var page = alertQueryUseCase.getAlertPage(filter, first, offset);
        var edges = IntStream.range(0, page.alerts().size())
                .mapToObj(i -> new AlertEdge(OffsetCursor.encode(page.startOffset() + i), page.alerts().get(i)))
                .toList();
        String endCursor = edges.isEmpty() ? null : edges.get(edges.size() - 1).cursor();
        return new AlertConnection(edges, new PageInfo(page.hasNextPage(), endCursor), page.totalCount());
    }

    @QueryMapping
    public List<AlertRuleView> alertRules(@Argument Boolean active) {
        return alertQueryUseCase.listRules(active);
    }

    @SchemaMapping(typeName = "Alert", field = "device")
    public CompletableFuture<DeviceView> alertDevice(AlertView alert, DataLoader<UUID, DeviceView> deviceLoader) {
        return deviceLoader.load(alert.deviceId());
    }

    @SchemaMapping(typeName = "AlertRule", field = "device")
    public CompletableFuture<DeviceView> ruleDevice(AlertRuleView rule, DataLoader<UUID, DeviceView> deviceLoader) {
        return rule.deviceId() == null ? CompletableFuture.completedFuture(null) : deviceLoader.load(rule.deviceId());
    }

    @BatchMapping(typeName = "AlertRule", field = "group")
    public Map<AlertRuleView, DeviceGroupView> group(List<AlertRuleView> rules) {
        var groupIds = rules.stream().map(AlertRuleView::groupId).filter(Objects::nonNull).distinct().toList();
        var groups = groupIds.isEmpty() ? Map.<UUID, DeviceGroupView>of() : deviceQueryUseCase.getGroupsByIds(groupIds);
        return mapByKey(rules, rule -> groups.get(rule.groupId()));
    }

    @BatchMapping(typeName = "Alert", field = "rule")
    public Map<AlertView, AlertRuleView> rule(List<AlertView> alerts) {
        var rules = alertQueryUseCase.getRulesByIds(alerts.stream().map(AlertView::ruleId).distinct().toList());
        return mapByKey(alerts, alert -> rules.get(alert.ruleId()));
    }

    @BatchMapping(typeName = "Alert", field = "notification")
    public Map<AlertView, NotificationView> notification(List<AlertView> alerts) {
        var notifications = notificationQueryUseCase.getNotificationsByAlertIds(
                alerts.stream().map(AlertView::id).distinct().toList());
        return mapByKey(alerts, alert -> notifications.get(alert.id()));
    }

    private <K, V> Map<K, V> mapByKey(List<K> keys, Function<K, V> valueFor) {
        Map<K, V> result = new HashMap<>();
        for (K key : keys) {
            V value = valueFor.apply(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }
}
