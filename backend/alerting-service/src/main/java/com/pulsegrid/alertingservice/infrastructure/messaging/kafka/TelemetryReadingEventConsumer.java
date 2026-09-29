package com.pulsegrid.alertingservice.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.alertingservice.application.command.EvaluateTelemetryReadingCommand;
import com.pulsegrid.alertingservice.application.port.in.EvaluateTelemetryReadingUseCase;
import com.pulsegrid.alertingservice.infrastructure.messaging.kafka.event.TelemetryReadingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelemetryReadingEventConsumer {

    private final EvaluateTelemetryReadingUseCase evaluateTelemetryReadingUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.telemetry-events-topic}")
    public void consume(String message) throws JsonProcessingException {
        TelemetryReadingEvent event = objectMapper.readValue(message, TelemetryReadingEvent.class);
        evaluateTelemetryReadingUseCase.evaluate(new EvaluateTelemetryReadingCommand(
                event.deviceId(),
                event.metricType(),
                event.value(),
                event.recordedAt()
        ));
    }
}
