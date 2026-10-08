package com.pulsegrid.notificationworkers.infrastructure.notification.email;

import com.pulsegrid.notificationworkers.application.error.NotificationDeliveryUnavailableException;
import com.pulsegrid.notificationworkers.application.port.out.AlertNotification;
import com.pulsegrid.notificationworkers.infrastructure.notification.email.config.EmailNotificationProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailAlertNotificationSenderAdapterTest {

    @Mock
    private ResilientMailSender resilientMailSender;

    private EmailAlertNotificationSenderAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EmailAlertNotificationSenderAdapter(resilientMailSender,
                new EmailNotificationProperties("alerts@pulsegrid.local", "ops-team@pulsegrid.local"));
    }

    @Test
    void shouldBuildEmailFromNotification() {
        AlertNotification notification = notification();

        adapter.send(notification);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(resilientMailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo("alerts@pulsegrid.local");
        assertThat(message.getTo()).containsExactly("ops-team@pulsegrid.local");
        assertThat(message.getSubject()).isEqualTo("[CRITICAL] Alerta en Horno 3: TEMPERATURE");
        assertThat(message.getText()).contains("Valor registrado: 92.5", notification.alertId().toString());
    }

    @Test
    void shouldTranslateSendFailureToDeliveryUnavailable() {
        doThrow(new MailSendException("Connection refused")).when(resilientMailSender).send(any());

        assertThatThrownBy(() -> adapter.send(notification()))
                .isInstanceOf(NotificationDeliveryUnavailableException.class)
                .hasCauseInstanceOf(MailSendException.class);
    }

    @Test
    void shouldTranslateOpenCircuitToDeliveryUnavailable() {
        doThrow(CallNotPermittedException.createCallNotPermittedException(CircuitBreaker.ofDefaults("email")))
                .when(resilientMailSender).send(any());

        assertThatThrownBy(() -> adapter.send(notification()))
                .isInstanceOf(NotificationDeliveryUnavailableException.class);
    }

    @Test
    void shouldNotTranslateBrokenMessageFailures() {
        doThrow(new MailParseException("Dirección inválida")).when(resilientMailSender).send(any());

        assertThatThrownBy(() -> adapter.send(notification()))
                .isInstanceOf(MailParseException.class);
    }

    private AlertNotification notification() {
        return new AlertNotification(UUID.randomUUID(), "Horno 3", "TEMPERATURE", "GREATER_THAN", 80.0, 92.5,
                "CRITICAL", Instant.parse("2026-10-01T14:00:00Z"));
    }
}
