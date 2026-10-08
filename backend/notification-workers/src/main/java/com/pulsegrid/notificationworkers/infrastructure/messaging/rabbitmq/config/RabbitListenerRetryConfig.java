package com.pulsegrid.notificationworkers.infrastructure.messaging.rabbitmq.config;

import com.pulsegrid.notificationworkers.application.error.NotificationDeliveryUnavailableException;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.Map;

@Configuration
public class RabbitListenerRetryConfig {

    @Bean
    public MessageRecoverer alertNotificationMessageRecoverer() {
        return new AlertNotificationMessageRecoverer();
    }

    @Bean
    public RabbitRetryTemplateCustomizer listenerRetryPolicyCustomizer(RabbitProperties rabbitProperties) {
        int maxAttempts = rabbitProperties.getListener().getSimple().getRetry().getMaxAttempts();
        return (target, retryTemplate) -> {
            if (target == RabbitRetryTemplateCustomizer.Target.LISTENER) {
                retryTemplate.setRetryPolicy(new SimpleRetryPolicy(
                        maxAttempts,
                        Map.of(NotificationDeliveryUnavailableException.class, false),
                        true,
                        true));
            }
        };
    }
}
