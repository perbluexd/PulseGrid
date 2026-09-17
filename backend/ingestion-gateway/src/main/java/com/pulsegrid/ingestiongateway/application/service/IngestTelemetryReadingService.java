package com.pulsegrid.ingestiongateway.application.service;

import com.pulsegrid.ingestiongateway.application.command.IngestTelemetryReadingCommand;
import com.pulsegrid.ingestiongateway.application.command.ResolveDeviceByApiKeyCommand;
import com.pulsegrid.ingestiongateway.application.event.TelemetryReadingEvent;
import com.pulsegrid.ingestiongateway.application.port.in.IngestTelemetryReadingUseCase;
import com.pulsegrid.ingestiongateway.application.port.in.ResolveDeviceByApiKeyUseCase;
import com.pulsegrid.ingestiongateway.application.port.out.TelemetryEventPublisherPort;
import com.pulsegrid.ingestiongateway.application.port.result.IngestTelemetryReadingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IngestTelemetryReadingService implements IngestTelemetryReadingUseCase {

    private final ResolveDeviceByApiKeyUseCase resolveDeviceByApiKeyUseCase;
    private final TelemetryEventPublisherPort telemetryEventPublisherPort;

    @Override
    public Mono<IngestTelemetryReadingResult> ingest(IngestTelemetryReadingCommand command) {
        return resolveDeviceByApiKeyUseCase.resolve(new ResolveDeviceByApiKeyCommand(command.apiKey()))
                .flatMap(resolved -> {
                    var event = new TelemetryReadingEvent(
                            resolved.deviceId(),
                            command.metricType(),
                            command.value(),
                            command.recordedAt()
                    );
                    return telemetryEventPublisherPort.publish(event)
                            .thenReturn(resolved.deviceId());
                })
                .map(IngestTelemetryReadingResult::new);
    }
}
