package com.pulsegrid.queryapi.api.graphql;

import com.pulsegrid.queryapi.api.graphql.controller.AlertGraphQlController;
import com.pulsegrid.queryapi.api.graphql.controller.DeviceGraphQlController;
import com.pulsegrid.queryapi.api.graphql.controller.NotificationGraphQlController;
import com.pulsegrid.queryapi.api.graphql.controller.TelemetryGraphQlController;
import com.pulsegrid.queryapi.api.graphql.error.GraphQlExceptionResolver;
import com.pulsegrid.queryapi.application.error.DownstreamServiceUnavailableException;
import com.pulsegrid.queryapi.application.model.AlertFilter;
import com.pulsegrid.queryapi.application.model.AlertPage;
import com.pulsegrid.queryapi.application.model.AlertSeverity;
import com.pulsegrid.queryapi.application.model.AlertStatus;
import com.pulsegrid.queryapi.application.model.AlertView;
import com.pulsegrid.queryapi.application.model.BucketSize;
import com.pulsegrid.queryapi.application.model.DeviceStatus;
import com.pulsegrid.queryapi.application.model.DeviceView;
import com.pulsegrid.queryapi.application.model.NotificationView;
import com.pulsegrid.queryapi.application.model.ReadingView;
import com.pulsegrid.queryapi.application.port.in.AlertQueryUseCase;
import com.pulsegrid.queryapi.application.port.in.DeviceQueryUseCase;
import com.pulsegrid.queryapi.application.port.in.NotificationQueryUseCase;
import com.pulsegrid.queryapi.application.port.in.TelemetryQueryUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@GraphQlTest(controllers = {
        DeviceGraphQlController.class,
        TelemetryGraphQlController.class,
        AlertGraphQlController.class,
        NotificationGraphQlController.class
})
@Import({GraphQlConfig.class, GraphQlExceptionResolver.class, DeviceBatchLoaderConfig.class})
@TestPropertySource(properties = {"app.graphql.max-query-depth=8", "app.graphql.max-query-complexity=300"})
class QueryApiGraphQlTest {

    private static final Instant CREATED_AT = Instant.parse("2026-10-01T10:00:00Z");

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private DeviceQueryUseCase deviceQueryUseCase;

    @MockitoBean
    private TelemetryQueryUseCase telemetryQueryUseCase;

    @MockitoBean
    private AlertQueryUseCase alertQueryUseCase;

    @MockitoBean
    private NotificationQueryUseCase notificationQueryUseCase;

    @Test
    void shouldReturnOnlyTheRequestedFields() {
        var device = device("sensor-01");
        when(deviceQueryUseCase.listDevices(null)).thenReturn(List.of(device));

        graphQlTester.document("{ devices { name } }")
                .execute()
                .path("devices[0].name").entity(String.class).isEqualTo("sensor-01")
                .path("devices[0].status").pathDoesNotExist();

        verify(telemetryQueryUseCase, never()).getLatestReadingsByDevice(anyCollection());
    }

    @Test
    void shouldLoadLatestReadingsForAllDevicesInASingleBatch() {
        var first = device("sensor-01");
        var second = device("sensor-02");
        var third = device("sensor-03");
        when(deviceQueryUseCase.listDevices(null)).thenReturn(List.of(first, second, third));
        when(telemetryQueryUseCase.getLatestReadingsByDevice(anyCollection())).thenReturn(Map.of(
                first.id(), List.of(new ReadingView(first.id(), "TEMPERATURE", 23.5, CREATED_AT)),
                third.id(), List.of(new ReadingView(third.id(), "HUMIDITY", 60.0, CREATED_AT))));

        graphQlTester.document("{ devices { name latestReadings { metricType value } } }")
                .execute()
                .path("devices[0].latestReadings[0].value").entity(Double.class).isEqualTo(23.5)
                .path("devices[1].latestReadings").entityList(Object.class).hasSize(0)
                .path("devices[2].latestReadings[0].metricType").entity(String.class).isEqualTo("HUMIDITY");

        verify(telemetryQueryUseCase, times(1)).getLatestReadingsByDevice(List.of(first.id(), second.id(), third.id()));
    }

    @Test
    void shouldResolveAlertDevicesThroughOneSharedDataLoaderCall() {
        var deviceA = device("sensor-A");
        var deviceB = device("sensor-B");
        var alerts = List.of(alert(deviceA.id()), alert(deviceB.id()), alert(deviceA.id()));
        when(alertQueryUseCase.getAlertPage(any(), eq(20), eq(0))).thenReturn(new AlertPage(alerts, 0, false, 3));
        when(deviceQueryUseCase.getDevicesByIds(anyCollection()))
                .thenReturn(Map.of(deviceA.id(), deviceA, deviceB.id(), deviceB));

        graphQlTester.document("{ alerts { edges { node { device { name } } } } }")
                .execute()
                .path("alerts.edges[0].node.device.name").entity(String.class).isEqualTo("sensor-A")
                .path("alerts.edges[1].node.device.name").entity(String.class).isEqualTo("sensor-B")
                .path("alerts.edges[2].node.device.name").entity(String.class).isEqualTo("sensor-A");

        verify(deviceQueryUseCase, times(1)).getDevicesByIds(anyCollection());
    }

    @Test
    void shouldReturnPartialDataWhenOneDownstreamServiceIsUnavailable() {
        when(deviceQueryUseCase.listDevices(null)).thenReturn(List.of(device("sensor-01")));
        when(telemetryQueryUseCase.getLatestReadingsByDevice(anyCollection()))
                .thenThrow(new DownstreamServiceUnavailableException("aggregation-service", new RuntimeException()));

        graphQlTester.document("{ devices { name latestReadings { value } } }")
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).hasSize(1);
                    assertThat(errors.get(0).getErrorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
                    assertThat(errors.get(0).getExtensions()).containsEntry("code", "DOWNSTREAM_UNAVAILABLE")
                            .containsEntry("service", "aggregation-service");
                    assertThat(errors.get(0).getPath()).isEqualTo("devices[0].latestReadings");
                })
                .path("devices[0].name").entity(String.class).isEqualTo("sensor-01")
                .path("devices[0].latestReadings").valueIsNull();
    }

    @Test
    void shouldPaginateAlertsWithOpaqueCursors() {
        var page1 = List.of(alert(UUID.randomUUID()), alert(UUID.randomUUID()));
        when(alertQueryUseCase.getAlertPage(any(), eq(2), eq(0))).thenReturn(new AlertPage(page1, 0, true, 5));
        var page2 = List.of(alert(UUID.randomUUID()), alert(UUID.randomUUID()));
        when(alertQueryUseCase.getAlertPage(any(), eq(2), eq(2))).thenReturn(new AlertPage(page2, 2, true, 5));

        String endCursor = graphQlTester.document("{ alerts(first: 2) { totalCount pageInfo { hasNextPage endCursor } } }")
                .execute()
                .path("alerts.totalCount").entity(Integer.class).isEqualTo(5)
                .path("alerts.pageInfo.hasNextPage").entity(Boolean.class).isEqualTo(true)
                .path("alerts.pageInfo.endCursor").entity(String.class).get();

        graphQlTester.document("query($after: String) { alerts(first: 2, after: $after) { edges { node { id } } } }")
                .variable("after", endCursor)
                .execute()
                .path("alerts.edges[0].node.id").entity(String.class).isEqualTo(page2.get(0).id().toString());
    }

    @Test
    void shouldPassFilterArgumentsAsInputObject() {
        UUID deviceId = UUID.randomUUID();
        when(alertQueryUseCase.getAlertPage(any(), eq(20), eq(0))).thenReturn(new AlertPage(List.of(), 0, false, 0));

        graphQlTester.document("{ alerts(filter: {status: TRIGGERED, deviceId: \"" + deviceId + "\"}) { totalCount } }")
                .execute()
                .path("alerts.totalCount").entity(Integer.class).isEqualTo(0);

        verify(alertQueryUseCase).getAlertPage(new AlertFilter(AlertStatus.TRIGGERED, null, deviceId), 20, 0);
    }

    @Test
    void shouldParseDateTimeArgumentsIntoInstants() {
        UUID deviceId = UUID.randomUUID();
        Instant from = Instant.parse("2026-10-08T10:00:00Z");
        Instant to = Instant.parse("2026-10-08T11:00:00Z");
        when(telemetryQueryUseCase.getHistory(deviceId, "TEMPERATURE", from, to, BucketSize.FIVE_MINUTES))
                .thenReturn(List.of());

        graphQlTester.document("""
                        { telemetryHistory(deviceId: "%s", metricType: "TEMPERATURE",
                            from: "2026-10-08T10:00:00Z", to: "2026-10-08T11:00:00Z", bucketSize: FIVE_MINUTES) { avg } }
                        """.formatted(deviceId))
                .execute()
                .path("telemetryHistory").entityList(Object.class).hasSize(0);
    }

    @Test
    void shouldRejectInvalidDateTimeLiteral() {
        graphQlTester.document("""
                        { telemetryHistory(deviceId: "%s", metricType: "TEMPERATURE",
                            from: "ayer", to: "2026-10-08T11:00:00Z") { avg } }
                        """.formatted(UUID.randomUUID()))
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors.get(0).getMessage()).contains("DateTime inválido"));
    }

    @Test
    void shouldRejectQueriesDeeperThanTheConfiguredLimit() {
        graphQlTester.document("""
                        { alerts { edges { node { notification { alert { rule { device {
                            latestReadings { value } } } } } } } } }
                        """)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors)
                        .anySatisfy(error -> assertThat(error.getMessage()).containsIgnoringCase("depth")));

        verify(alertQueryUseCase, never()).getAlertPage(any(), any(Integer.class), any(Integer.class));
    }

    @Test
    void shouldResolveNotificationForEachAlertInOneBatch() {
        var alert1 = alert(UUID.randomUUID());
        var alert2 = alert(UUID.randomUUID());
        when(alertQueryUseCase.getAlertPage(any(), eq(20), eq(0))).thenReturn(new AlertPage(List.of(alert1, alert2), 0, false, 2));
        when(notificationQueryUseCase.getNotificationsByAlertIds(anyCollection()))
                .thenReturn(Map.of(alert1.id(), new NotificationView(alert1.id(), CREATED_AT)));

        graphQlTester.document("{ alerts { edges { node { notification { notifiedAt } } } } }")
                .execute()
                .path("alerts.edges[0].node.notification.notifiedAt").entity(String.class).isEqualTo("2026-10-01T10:00:00Z")
                .path("alerts.edges[1].node.notification").valueIsNull();

        verify(notificationQueryUseCase, times(1)).getNotificationsByAlertIds(anyCollection());
    }

    private DeviceView device(String name) {
        return new DeviceView(UUID.randomUUID(), name, "SENSOR", DeviceStatus.ACTIVE, "Planta Norte", CREATED_AT, null);
    }

    private AlertView alert(UUID deviceId) {
        return new AlertView(UUID.randomUUID(), UUID.randomUUID(), deviceId, 95.5, AlertSeverity.CRITICAL,
                AlertStatus.TRIGGERED, CREATED_AT, null);
    }
}
