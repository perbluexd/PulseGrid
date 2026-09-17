package com.pulsegrid.ingestiongateway.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.ingestiongateway.application.event.TelemetryReadingEvent;
import com.pulsegrid.ingestiongateway.application.port.out.TelemetryEventPublisherPort;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Component
public class TelemetryEventPublisherAdapter implements TelemetryEventPublisherPort {

    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper;
    private final String topic;

    public TelemetryEventPublisherAdapter(KafkaSender<String, String> kafkaSender,
                                           ObjectMapper objectMapper,
                                           @Value("${app.kafka.telemetry-events-topic}") String topic) {
        this.kafkaSender = kafkaSender;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public Mono<Void> publish(TelemetryReadingEvent event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .map(json -> SenderRecord.create(
                        new ProducerRecord<>(topic, event.deviceId().toString(), json), null))
                .flatMap(record -> kafkaSender.send(Mono.just(record)).then());
    }
}
