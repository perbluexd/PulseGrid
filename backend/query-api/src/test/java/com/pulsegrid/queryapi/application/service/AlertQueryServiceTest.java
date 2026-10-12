package com.pulsegrid.queryapi.application.service;

import com.pulsegrid.queryapi.application.error.InvalidQueryArgumentException;
import com.pulsegrid.queryapi.application.model.AlertFilter;
import com.pulsegrid.queryapi.application.model.AlertSeverity;
import com.pulsegrid.queryapi.application.model.AlertStatus;
import com.pulsegrid.queryapi.application.model.AlertView;
import com.pulsegrid.queryapi.application.port.out.AlertingPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertQueryServiceTest {

    @Mock
    private AlertingPort alertingPort;

    @InjectMocks
    private AlertQueryService alertQueryService;

    @Test
    void shouldSliceTheRequestedPage() {
        var alerts = alerts(5);
        when(alertingPort.findAlerts(AlertFilter.none())).thenReturn(alerts);

        var page = alertQueryService.getAlertPage(null, 2, 2);

        assertThat(page.alerts()).containsExactly(alerts.get(2), alerts.get(3));
        assertThat(page.startOffset()).isEqualTo(2);
        assertThat(page.hasNextPage()).isTrue();
        assertThat(page.totalCount()).isEqualTo(5);
    }

    @Test
    void shouldReportNoNextPageOnTheLastPage() {
        when(alertingPort.findAlerts(AlertFilter.none())).thenReturn(alerts(3));

        var page = alertQueryService.getAlertPage(null, 2, 2);

        assertThat(page.alerts()).hasSize(1);
        assertThat(page.hasNextPage()).isFalse();
    }

    @Test
    void shouldReturnEmptyPageWhenOffsetIsBeyondTheEnd() {
        when(alertingPort.findAlerts(AlertFilter.none())).thenReturn(alerts(3));

        var page = alertQueryService.getAlertPage(null, 2, 10);

        assertThat(page.alerts()).isEmpty();
        assertThat(page.hasNextPage()).isFalse();
    }

    @Test
    void shouldRejectPageSizeOutOfRange() {
        assertThatThrownBy(() -> alertQueryService.getAlertPage(null, 101, 0))
                .isInstanceOf(InvalidQueryArgumentException.class);
        assertThatThrownBy(() -> alertQueryService.getAlertPage(null, 0, 0))
                .isInstanceOf(InvalidQueryArgumentException.class);
    }

    private List<AlertView> alerts(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new AlertView(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), i,
                        AlertSeverity.WARNING, AlertStatus.TRIGGERED, Instant.parse("2026-10-08T10:00:00Z"), null))
                .toList();
    }
}
