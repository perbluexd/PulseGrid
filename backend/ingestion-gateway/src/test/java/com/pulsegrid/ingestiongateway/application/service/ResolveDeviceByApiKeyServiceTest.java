package com.pulsegrid.ingestiongateway.application.service;

import com.pulsegrid.ingestiongateway.application.command.ResolveDeviceByApiKeyCommand;
import com.pulsegrid.ingestiongateway.application.error.DeviceNotActiveException;
import com.pulsegrid.ingestiongateway.application.error.InvalidApiKeyException;
import com.pulsegrid.ingestiongateway.application.port.out.DeviceApiKeyCachePort;
import com.pulsegrid.ingestiongateway.application.port.out.DeviceRegistryPort;
import com.pulsegrid.ingestiongateway.application.port.result.ResolveDeviceByApiKeyResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolveDeviceByApiKeyServiceTest {

    private static final String API_KEY = "raw-api-key";
    private static final UUID DEVICE_ID = UUID.randomUUID();

    @Mock
    private DeviceApiKeyCachePort deviceApiKeyCachePort;

    @Mock
    private DeviceRegistryPort deviceRegistryPort;

    @InjectMocks
    private ResolveDeviceByApiKeyService resolveDeviceByApiKeyService;

    @Test
    void shouldReturnDeviceIdFromCacheWithoutSubscribingToDeviceRegistry() {
        when(deviceApiKeyCachePort.get(API_KEY)).thenReturn(Mono.just(DEVICE_ID));
        when(deviceRegistryPort.resolveByApiKey(API_KEY))
                .thenReturn(Mono.error(new AssertionError("deviceRegistryPort should never be subscribed on a cache hit")));

        StepVerifier.create(resolveDeviceByApiKeyService.resolve(new ResolveDeviceByApiKeyCommand(API_KEY)))
                .expectNext(new ResolveDeviceByApiKeyResult(DEVICE_ID))
                .verifyComplete();

        verify(deviceApiKeyCachePort, never()).put(any(), any());
    }

    @Test
    void shouldResolveFromDeviceRegistryAndPopulateCacheOnCacheMiss() {
        when(deviceApiKeyCachePort.get(API_KEY)).thenReturn(Mono.empty());
        when(deviceRegistryPort.resolveByApiKey(API_KEY)).thenReturn(Mono.just(DEVICE_ID));
        when(deviceApiKeyCachePort.put(API_KEY, DEVICE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(resolveDeviceByApiKeyService.resolve(new ResolveDeviceByApiKeyCommand(API_KEY)))
                .expectNext(new ResolveDeviceByApiKeyResult(DEVICE_ID))
                .verifyComplete();

        verify(deviceApiKeyCachePort).put(API_KEY, DEVICE_ID);
    }

    @Test
    void shouldPropagateInvalidApiKeyExceptionWithoutPopulatingCache() {
        when(deviceApiKeyCachePort.get(API_KEY)).thenReturn(Mono.empty());
        when(deviceRegistryPort.resolveByApiKey(API_KEY)).thenReturn(Mono.error(new InvalidApiKeyException()));

        StepVerifier.create(resolveDeviceByApiKeyService.resolve(new ResolveDeviceByApiKeyCommand(API_KEY)))
                .expectError(InvalidApiKeyException.class)
                .verify();

        verify(deviceApiKeyCachePort, never()).put(any(), any());
    }

    @Test
    void shouldPropagateDeviceNotActiveExceptionWithoutPopulatingCache() {
        when(deviceApiKeyCachePort.get(API_KEY)).thenReturn(Mono.empty());
        when(deviceRegistryPort.resolveByApiKey(API_KEY)).thenReturn(Mono.error(new DeviceNotActiveException()));

        StepVerifier.create(resolveDeviceByApiKeyService.resolve(new ResolveDeviceByApiKeyCommand(API_KEY)))
                .expectError(DeviceNotActiveException.class)
                .verify();

        verify(deviceApiKeyCachePort, never()).put(any(), any());
    }
}
