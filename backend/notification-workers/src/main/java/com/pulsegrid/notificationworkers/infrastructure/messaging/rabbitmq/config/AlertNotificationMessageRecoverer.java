package com.pulsegrid.notificationworkers.infrastructure.messaging.rabbitmq.config;

import com.pulsegrid.notificationworkers.application.error.NotificationDeliveryUnavailableException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.ImmediateRequeueMessageRecoverer;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;

public class AlertNotificationMessageRecoverer implements MessageRecoverer {

    private final MessageRecoverer requeueRecoverer = new ImmediateRequeueMessageRecoverer();
    private final MessageRecoverer deadLetterRecoverer = new RejectAndDontRequeueRecoverer();

    @Override
    public void recover(Message message, Throwable cause) {
        if (isDeliveryUnavailable(cause)) {
            requeueRecoverer.recover(message, cause);
            return;
        }
        deadLetterRecoverer.recover(message, cause);
    }

    private boolean isDeliveryUnavailable(Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (current instanceof NotificationDeliveryUnavailableException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
