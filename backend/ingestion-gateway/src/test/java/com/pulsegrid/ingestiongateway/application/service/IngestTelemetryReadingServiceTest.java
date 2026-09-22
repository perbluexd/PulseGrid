package com.pulsegrid.ingestiongateway.application.service;

import com.pulsegrid.ingestiongateway.application.command.IngestTelemetryReadingCommand;
import com.pulsegrid.ingestiongateway.application.command.ResolveDeviceByApiKeyCommand;
import com.pulsegrid.ingestiongateway.application.error.InvalidApiKeyException;
import com.pulsegrid.ingestiongateway.application.event.TelemetryReadingEvent;
import com.pulsegrid.ingestiongateway.application.port.in.ResolveDeviceByApiKeyUseCase;
import com.pulsegrid.ingestiongateway.application.port.out.TelemetryEventPublisherPort;
import com.pulsegrid.ingestiongateway.application.port.result.IngestTelemetryReadingResult;
import com.pulsegrid.ingestiongateway.application.port.result.ResolveDeviceByApiKeyResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestTelemetryReadingServiceTest {

    private static final String API_KEY = "raw-api-key";
    private static final UUID DEVICE_ID = UUID.randomUUID();
    private static final Instant RECORDED_AT = Instant.parse("2026-09-20T10:15:30Z");

    @Mock
    private ResolveDeviceByApiKeyUseCase resolveDeviceByApiKeyUseCase;

    @Mock
    private TelemetryEventPublisherPort telemetryEventPublisherPort;

    @InjectMocks
    private IngestTelemetryReadingService ingestTelemetryReadingService;

    @Test
    void shouldPublishEventAndReturnDeviceIdWhenResolutionSucceeds() {
        var command = new IngestTelemetryReadingCommand(API_KEY, "TEMPERATURE", 23.5, RECORDED_AT);

        when(resolveDeviceByApiKeyUseCase.resolve(new ResolveDeviceByApiKeyCommand(API_KEY)))
                .thenReturn(Mono.just(new ResolveDeviceByApiKeyResult(DEVICE_ID)));
        when(telemetryEventPublisherPort.publish(any())).thenReturn(Mono.empty());

        StepVerifier.create(ingestTelemetryReadingService.ingest(command))
                .expectNext(new IngestTelemetryReadingResult(DEVICE_ID))
                .verifyComplete();

        ArgumentCaptor<TelemetryReadingEvent> eventCaptor = ArgumentCaptor.forClass(TelemetryReadingEvent.class);
        verify(telemetryEventPublisherPort).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue())
                .isEqualTo(new TelemetryReadingEvent(DEVICE_ID, "TEMPERATURE", 23.5, RECORDED_AT));
    }

    @Test
    void shouldPropagateInvalidApiKeyExceptionWithoutPublishing() {
        var command = new IngestTelemetryReadingCommand(API_KEY, "TEMPERATURE", 23.5, RECORDED_AT);

        when(resolveDeviceByApiKeyUseCase.resolve(new ResolveDeviceByApiKeyCommand(API_KEY)))
                .thenReturn(Mono.error(new InvalidApiKeyException()));

        StepVerifier.create(ingestTelemetryReadingService.ingest(command))
                .expectError(InvalidApiKeyException.class)
                .verify();

        verify(telemetryEventPublisherPort, never()).publish(any());
    }

    @Test
    void shouldPropagatePublishErrorWhenPublishingFails() {
        var command = new IngestTelemetryReadingCommand(API_KEY, "TEMPERATURE", 23.5, RECORDED_AT);

        when(resolveDeviceByApiKeyUseCase.resolve(new ResolveDeviceByApiKeyCommand(API_KEY)))
                .thenReturn(Mono.just(new ResolveDeviceByApiKeyResult(DEVICE_ID)));
        when(telemetryEventPublisherPort.publish(any()))
                .thenReturn(Mono.error(new RuntimeException("kafka down")));

        StepVerifier.create(ingestTelemetryReadingService.ingest(command))
                .expectError(RuntimeException.class)
                .verify();
    }
}
