package com.pulsegrid.alertingservice.infrastructure.messaging.kafka.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(2000L, 5L));
        errorHandler.addNotRetryableExceptions(JsonProcessingException.class, IllegalArgumentException.class);
        return errorHandler;
    }
}
