package com.pulsegrid.alertingservice.infrastructure.messaging.rabbitmq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RabbitMqProperties.class)
public class RabbitMqConfig {

    @Bean
    public DirectExchange alertNotificationsExchange(RabbitMqProperties properties) {
        return ExchangeBuilder.directExchange(properties.exchange()).durable(true).build();
    }

    @Bean
    public Queue alertNotificationsQueue(RabbitMqProperties properties) {
        return QueueBuilder.durable(properties.queue())
                .deadLetterExchange(properties.deadLetterExchange())
                .build();
    }

    @Bean
    public Binding alertNotificationsBinding(Queue alertNotificationsQueue, DirectExchange alertNotificationsExchange,
                                             RabbitMqProperties properties) {
        return BindingBuilder.bind(alertNotificationsQueue).to(alertNotificationsExchange).with(properties.routingKey());
    }

    @Bean
    public FanoutExchange alertNotificationsDeadLetterExchange(RabbitMqProperties properties) {
        return ExchangeBuilder.fanoutExchange(properties.deadLetterExchange()).durable(true).build();
    }

    @Bean
    public Queue alertNotificationsDeadLetterQueue(RabbitMqProperties properties) {
        return QueueBuilder.durable(properties.deadLetterQueue()).build();
    }

    @Bean
    public Binding alertNotificationsDeadLetterBinding(Queue alertNotificationsDeadLetterQueue,
                                                       FanoutExchange alertNotificationsDeadLetterExchange) {
        return BindingBuilder.bind(alertNotificationsDeadLetterQueue).to(alertNotificationsDeadLetterExchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
