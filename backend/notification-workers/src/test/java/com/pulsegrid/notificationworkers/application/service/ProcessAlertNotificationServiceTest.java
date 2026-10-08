package com.pulsegrid.notificationworkers.application.service;

import com.pulsegrid.notificationworkers.application.command.ProcessAlertNotificationCommand;
import com.pulsegrid.notificationworkers.application.error.NotificationDeliveryUnavailableException;
import com.pulsegrid.notificationworkers.application.port.out.AlertNotification;
import com.pulsegrid.notificationworkers.application.port.out.AlertNotificationSenderPort;
import com.pulsegrid.notificationworkers.application.port.out.ProcessedNotificationRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessAlertNotificationServiceTest {

    @Mock
    private ProcessedNotificationRepositoryPort processedNotificationRepositoryPort;

    @Mock
    private AlertNotificationSenderPort alertNotificationSenderPort;

    @InjectMocks
    private ProcessAlertNotificationService service;

    @Test
    void shouldSendNotificationWhenAlertIsProcessedForTheFirstTime() {
        ProcessAlertNotificationCommand command = command();
        when(processedNotificationRepositoryPort.markAsProcessed(eq(command.alertId()), any())).thenReturn(true);

        service.process(command);

        ArgumentCaptor<AlertNotification> captor = ArgumentCaptor.forClass(AlertNotification.class);
        verify(alertNotificationSenderPort).send(captor.capture());
        assertThat(captor.getValue().alertId()).isEqualTo(command.alertId());
        assertThat(captor.getValue().deviceName()).isEqualTo("Horno 3");
        assertThat(captor.getValue().triggeredValue()).isEqualTo(92.5);
    }

    @Test
    void shouldNotSendNotificationWhenAlertWasAlreadyProcessed() {
        ProcessAlertNotificationCommand command = command();
        when(processedNotificationRepositoryPort.markAsProcessed(eq(command.alertId()), any())).thenReturn(false);

        service.process(command);

        verify(alertNotificationSenderPort, never()).send(any());
    }

    @Test
    void shouldPropagateDeliveryFailureSoTheTransactionRollsBack() {
        ProcessAlertNotificationCommand command = command();
        when(processedNotificationRepositoryPort.markAsProcessed(eq(command.alertId()), any())).thenReturn(true);
        doThrow(new NotificationDeliveryUnavailableException("SMTP caído", null))
                .when(alertNotificationSenderPort).send(any());

        assertThatThrownBy(() -> service.process(command))
                .isInstanceOf(NotificationDeliveryUnavailableException.class);
    }

    private ProcessAlertNotificationCommand command() {
        return new ProcessAlertNotificationCommand(UUID.randomUUID(), UUID.randomUUID(), "Horno 3", "TEMPERATURE",
                "GREATER_THAN", 80.0, 92.5, "CRITICAL", Instant.parse("2026-10-01T14:00:00Z"));
    }
}
