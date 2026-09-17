package com.pulsegrid.ingestiongateway.application.port.out;

import com.pulsegrid.ingestiongateway.application.event.TelemetryReadingEvent;
import reactor.core.publisher.Mono;

public interface TelemetryEventPublisherPort {
    Mono<Void> publish(TelemetryReadingEvent event);
}
