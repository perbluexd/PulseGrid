package com.pulsegrid.notificationworkers.infrastructure.messaging.rabbitmq;

import com.pulsegrid.notificationworkers.infrastructure.notification.email.ResilientMailSender;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailCircuitBreakerListenerController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RabbitListenerEndpointRegistry listenerRegistry;

    @PostConstruct
    void subscribe() {
        circuitBreakerRegistry.circuitBreaker(ResilientMailSender.EMAIL)
                .getEventPublisher()
                .onStateTransition(this::pauseOrResumeListener);
    }

    private void pauseOrResumeListener(CircuitBreakerOnStateTransitionEvent event) {
        CircuitBreaker.State toState = event.getStateTransition().getToState();
        log.warn("Circuit breaker '{}': {}", event.getCircuitBreakerName(), event.getStateTransition());

        MessageListenerContainer container = listenerRegistry.getListenerContainer(AlertNotificationListener.LISTENER_ID);
        if (toState == CircuitBreaker.State.OPEN) {
            Thread.ofVirtual().start(() -> {
                container.stop();
                log.warn("Listener pausado: los mensajes esperan en la cola hasta que el email vuelva");
            });
        } else if (toState == CircuitBreaker.State.HALF_OPEN || toState == CircuitBreaker.State.CLOSED) {
            if (!container.isRunning()) {
                container.start();
                log.info("Listener reanudado");
            }
        }
    }
}
