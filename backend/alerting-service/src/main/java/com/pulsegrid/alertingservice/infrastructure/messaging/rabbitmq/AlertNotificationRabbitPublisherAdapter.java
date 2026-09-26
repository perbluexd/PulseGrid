package com.pulsegrid.alertingservice.infrastructure.messaging.rabbitmq;

import com.pulsegrid.alertingservice.application.event.AlertTriggeredNotification;
import com.pulsegrid.alertingservice.application.port.out.AlertNotificationPublisherPort;
import com.pulsegrid.alertingservice.infrastructure.messaging.rabbitmq.config.RabbitMqProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class AlertNotificationRabbitPublisherAdapter implements AlertNotificationPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties properties;

    @Override
    public void publish(AlertTriggeredNotification notification) {
        CorrelationData correlationData = new CorrelationData(notification.alertId().toString());

        rabbitTemplate.convertAndSend(properties.exchange(), properties.routingKey(), notification, correlationData);

        awaitConfirmation(correlationData);
    }

    private void awaitConfirmation(CorrelationData correlationData) {
        try {
            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(properties.confirmTimeout().toMillis(), TimeUnit.MILLISECONDS);

            if (!confirm.isAck()) {
                throw new AmqpException("RabbitMQ rechazó el mensaje " + correlationData.getId() + ": " + confirm.getReason());
            }
            if (correlationData.getReturned() != null) {
                throw new AmqpException("RabbitMQ no pudo enrutar el mensaje " + correlationData.getId()
                        + ": " + correlationData.getReturned().getReplyText());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AmqpException("Interrumpido esperando la confirmación de RabbitMQ", ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new AmqpException("Sin confirmación de RabbitMQ para el mensaje " + correlationData.getId(), ex);
        }
    }
}
