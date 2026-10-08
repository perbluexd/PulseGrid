package com.pulsegrid.notificationworkers.infrastructure.messaging.rabbitmq.config;

import com.pulsegrid.notificationworkers.application.error.NotificationDeliveryUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateRequeueAmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertNotificationMessageRecovererTest {

    private final AlertNotificationMessageRecoverer recoverer = new AlertNotificationMessageRecoverer();
    private final Message message = new Message("{}".getBytes(), new MessageProperties());

    @Test
    void shouldRequeueWhenDeliveryChannelIsUnavailable() {
        Throwable cause = new ListenerExecutionFailedException("Listener failed",
                new NotificationDeliveryUnavailableException("SMTP caído", null), message);

        assertThatThrownBy(() -> recoverer.recover(message, cause))
                .isInstanceOf(ImmediateRequeueAmqpException.class);
    }

    @Test
    void shouldRejectWithoutRequeueForAnyOtherFailure() {
        Throwable cause = new ListenerExecutionFailedException("Listener failed",
                new IllegalStateException("Mensaje inválido"), message);

        assertThatThrownBy(() -> recoverer.recover(message, cause))
                .isInstanceOf(ListenerExecutionFailedException.class)
                .hasCauseInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
}
