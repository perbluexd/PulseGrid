package com.pulsegrid.aggregationservice.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsegrid.aggregationservice.application.command.RecordTelemetryReadingCommand;
import com.pulsegrid.aggregationservice.application.port.in.RecordTelemetryReadingUseCase;
import com.pulsegrid.aggregationservice.infrastructure.messaging.event.TelemetryReadingEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class TelemetryReadingEventConsumer {

    private final RecordTelemetryReadingUseCase recordTelemetryReadingUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.telemetry-events-topic}")
    public void consume(String message) throws JsonProcessingException {
        TelemetryReadingEvent event = objectMapper.readValue(message, TelemetryReadingEvent.class);
        recordTelemetryReadingUseCase.record(new RecordTelemetryReadingCommand(
                event.deviceId(),
                event.metricType(),
                event.value(),
                event.recordedAt()
        ));
    }
}
