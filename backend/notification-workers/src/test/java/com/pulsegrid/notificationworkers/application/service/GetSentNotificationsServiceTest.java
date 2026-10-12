package com.pulsegrid.notificationworkers.application.service;

import com.pulsegrid.notificationworkers.application.port.out.SentNotificationQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetSentNotificationsServiceTest {

    @Mock
    private SentNotificationQueryPort sentNotificationQueryPort;

    @InjectMocks
    private GetSentNotificationsService getSentNotificationsService;

    @Test
    void shouldNotQueryWhenNoAlertIdsAreGiven() {
        assertThat(getSentNotificationsService.getByAlertIds(List.of())).isEmpty();
        verify(sentNotificationQueryPort, never()).findByAlertIds(any());
    }

    @Test
    void shouldQueryEachAlertIdOnlyOnce() {
        UUID alertId = UUID.randomUUID();

        getSentNotificationsService.getByAlertIds(List.of(alertId, alertId));

        verify(sentNotificationQueryPort).findByAlertIds(List.of(alertId));
    }
}
