package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.RotateDeviceApiKeyCommand;
import com.pulsegrid.deviceregistry.application.error.ActiveApiKeyNotFoundException;
import com.pulsegrid.deviceregistry.application.error.DeviceNotFoundException;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyGeneratorPort;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.RotateDeviceApiKeyResult;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RotateDeviceApiKeyServiceTest {

    @Mock
    private DeviceRepositoryPort deviceRepositoryPort;

    @Mock
    private ApiKeyRepositoryPort apiKeyRepositoryPort;

    @Mock
    private ApiKeyGeneratorPort apiKeyGeneratorPort;

    @InjectMocks
    private RotateDeviceApiKeyService rotateDeviceApiKeyService;

    @Test
    void shouldRotateApiKeyWhenDeviceAndActiveKeyExist() {
        UUID deviceId = UUID.randomUUID();
        Device device = new Device(deviceId, "Sensor 1", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Lab", Instant.now(), Instant.now(), null);
        ApiKey currentKey = new ApiKey(UUID.randomUUID(), deviceId, "old-hash", ApiKeyStatus.ACTIVE,
                Instant.now(), null, null);

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));
        when(apiKeyRepositoryPort.findActiveByDeviceId(deviceId)).thenReturn(Optional.of(currentKey));
        when(apiKeyGeneratorPort.generateRawKey()).thenReturn("new-raw-key");
        when(apiKeyGeneratorPort.hash("new-raw-key")).thenReturn("new-hash");

        RotateDeviceApiKeyResult result = rotateDeviceApiKeyService.rotate(new RotateDeviceApiKeyCommand(deviceId));

        assertThat(result.deviceId()).isEqualTo(deviceId);
        assertThat(result.revokedApiKeyId()).isEqualTo(currentKey.getId());
        assertThat(result.newApiKey()).isEqualTo("new-raw-key");
        assertThat(currentKey.getStatus()).isEqualTo(ApiKeyStatus.REVOKED);
        verify(apiKeyRepositoryPort, times(2)).save(any());
    }

    @Test
    void shouldThrowWhenDeviceDoesNotExist() {
        UUID deviceId = UUID.randomUUID();
        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rotateDeviceApiKeyService.rotate(new RotateDeviceApiKeyCommand(deviceId)))
                .isInstanceOf(DeviceNotFoundException.class);

        verify(apiKeyRepositoryPort, never()).save(any());
    }

    @Test
    void shouldThrowWhenNoActiveApiKeyExists() {
        UUID deviceId = UUID.randomUUID();
        Device device = new Device(deviceId, "Sensor 1", DeviceType.SENSOR, DeviceStatus.ACTIVE,
                "Lab", Instant.now(), Instant.now(), null);

        when(deviceRepositoryPort.findById(deviceId)).thenReturn(Optional.of(device));
        when(apiKeyRepositoryPort.findActiveByDeviceId(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rotateDeviceApiKeyService.rotate(new RotateDeviceApiKeyCommand(deviceId)))
                .isInstanceOf(ActiveApiKeyNotFoundException.class);

        verify(apiKeyRepositoryPort, never()).save(any());
    }
}
