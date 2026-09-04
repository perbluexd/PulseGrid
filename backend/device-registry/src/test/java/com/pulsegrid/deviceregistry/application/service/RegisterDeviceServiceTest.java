package com.pulsegrid.deviceregistry.application.service;

import com.pulsegrid.deviceregistry.application.command.RegisterDeviceCommand;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyGeneratorPort;
import com.pulsegrid.deviceregistry.application.port.out.ApiKeyRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.out.DeviceRepositoryPort;
import com.pulsegrid.deviceregistry.application.port.result.RegisterDeviceResult;
import com.pulsegrid.deviceregistry.domain.model.DeviceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterDeviceServiceTest {

    @Mock
    private DeviceRepositoryPort deviceRepositoryPort;

    @Mock
    private ApiKeyRepositoryPort apiKeyRepositoryPort;

    @Mock
    private ApiKeyGeneratorPort apiKeyGeneratorPort;

    @InjectMocks
    private RegisterDeviceService registerDeviceService;

    @Test
    void shouldRegisterDeviceAndGenerateApiKey() {
        RegisterDeviceCommand command = new RegisterDeviceCommand("Sensor 1", DeviceType.SENSOR, "Lab");

        when(apiKeyGeneratorPort.generateRawKey()).thenReturn("raw-key-123");
        when(apiKeyGeneratorPort.hash("raw-key-123")).thenReturn("hashed-key-123");

        RegisterDeviceResult result = registerDeviceService.register(command);

        assertThat(result.name()).isEqualTo("Sensor 1");
        assertThat(result.type()).isEqualTo(DeviceType.SENSOR);
        assertThat(result.apiKey()).isEqualTo("raw-key-123");
        verify(deviceRepositoryPort).save(any());
        verify(apiKeyRepositoryPort).save(any());
    }
}
