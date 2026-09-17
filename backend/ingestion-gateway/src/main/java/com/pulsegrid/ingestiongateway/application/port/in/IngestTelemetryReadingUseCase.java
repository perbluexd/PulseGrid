package com.pulsegrid.ingestiongateway.application.port.in;

import com.pulsegrid.ingestiongateway.application.command.IngestTelemetryReadingCommand;
import com.pulsegrid.ingestiongateway.application.port.result.IngestTelemetryReadingResult;
import reactor.core.publisher.Mono;

public interface IngestTelemetryReadingUseCase {
    Mono<IngestTelemetryReadingResult> ingest(IngestTelemetryReadingCommand command);
}
