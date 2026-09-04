package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.ResolveDeviceByApiKeyCommand;
import com.pulsegrid.deviceregistry.application.error.DeviceNotActiveException;
import com.pulsegrid.deviceregistry.application.error.InvalidApiKeyException;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyGeneratorPort;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.ResolveDeviceByApiKeyResult;
import com.pulsegrid.deviceregistry.domain.model.ApiKey;
import com.pulsegrid.deviceregistry.domain.model.ApiKeyStatus;
import com.pulsegrid.deviceregistry.domain.model.Device;
import com.pulsegrid.deviceregistry.domain.model.DeviceStatus;
import com.pulsegrid.deviceregistry.domain.model.DeviceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolveDeviceByApiKeyServiceTest {

    @Mock
    private ApiKeyRepositoryPort apiKeyRepositoryPort;

    @Mock
    private ApiKeyGeneratorPort apiKeyGeneratorPort;

    @Mock
    private DeviceRepositoryPort deviceRepositoryPort;

    @InjectMocks
    private ResolveDeviceByApiKeyService resolveDeviceByApiKeyService;

    @Test
    void shouldResolveDeviceWhenApiKeyAndDeviceAreValid() {
        UUID deviceId = UUID.randomUUID();
        ApiKey apiKey = new ApiKey(UUID.randomUUID(), deviceId, "hashed-key", ApiKeyStatus.ACTIVE,
                Instant.now(), null, null);
        Device device = new Device(deviceId, "Sensor 1", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Lab", Instant.now(), Instant.now(), null);

        when(apiKeyGeneratorPort.hash("raw-key")).thenReturn("hashed-key");
        when(apiKeyRepositoryPort.findByHash("hashed-key")).thenReturn(Optional.of(apiKey));
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        ResolveDeviceByApiKeyResult result =
                resolveDeviceByApiKeyService.resolve(new ResolveDeviceByApiKeyCommand("raw-key"));

        assertThat(result.deviceId()).isEqualTo(deviceId);
        assertThat(result.name()).isEqualTo("Sensor 1");
        assertThat(result.type()).isEqualTo(DeviceType.SENSOR);
        assertThat(result.status()).isEqualTo(DeviceStatus.ACTIVE);
    }

    @Test
    void shouldThrowWhenApiKeyDoesNotExist() {
        when(apiKeyGeneratorPort.hash("raw-key")).thenReturn("hashed-key");
        when(apiKeyRepositoryPort.findByHash("hashed-key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolveDeviceByApiKeyService.resolve(new ResolveDeviceByApiKeyCommand("raw-key")))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    @Test
    void shouldThrowWhenApiKeyIsNotActive() {
        UUID deviceId = UUID.randomUUID();
        ApiKey apiKey = new ApiKey(UUID.randomUUID(), deviceId, "hashed-key", ApiKeyStatus.REVOKED,
                Instant.now(), null, Instant.now());

        when(apiKeyGeneratorPort.hash("raw-key")).thenReturn("hashed-key");
        when(apiKeyRepositoryPort.findByHash("hashed-key")).thenReturn(Optional.of(apiKey));

        assertThatThrownBy(() -> resolveDeviceByApiKeyService.resolve(new ResolveDeviceByApiKeyCommand("raw-key")))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    @Test
    void shouldThrowWhenDeviceDoesNotExist() {
        UUID deviceId = UUID.randomUUID();
        ApiKey apiKey = new ApiKey(UUID.randomUUID(), deviceId, "hashed-key", ApiKeyStatus.ACTIVE,
                Instant.now(), null, null);

        when(apiKeyGeneratorPort.hash("raw-key")).thenReturn("hashed-key");
        when(apiKeyRepositoryPort.findByHash("hashed-key")).thenReturn(Optional.of(apiKey));
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolveDeviceByApiKeyService.resolve(new ResolveDeviceByApiKeyCommand("raw-key")))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    @Test
    void shouldThrowWhenDeviceIsNotActive() {
        UUID deviceId = UUID.randomUUID();
        ApiKey apiKey = new ApiKey(UUID.randomUUID(), deviceId, "hashed-key", ApiKeyStatus.ACTIVE,
                Instant.now(), null, null);
        Device device = new Device(deviceId, "Sensor 1", DeviceType.SENSOR, DeviceStatus.INACTIVE,
                "Lab", Instant.now(), Instant.now(), null);

        when(apiKeyGeneratorPort.hash("raw-key")).thenReturn("hashed-key");
        when(apiKeyRepositoryPort.findByHash("hashed-key")).thenReturn(Optional.of(apiKey));
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> resolveDeviceByApiKeyService.resolve(new ResolveDeviceByApiKeyCommand("raw-key")))
                .isInstanceOf(DeviceNotActiveException.class);
    }
}
