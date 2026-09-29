package com.pulsegrid.alertingservice.infrastructure.client;

import com.pulsegrid.alertingservice.application.error.DeviceRegistryUnavailableException;
import com.pulsegrid.alertingservice.application.port.out.DeviceRegistryPort;
import com.pulsegrid.alertingservice.application.port.out.DeviceSummary;
import com.pulsegrid.alertingservice.infrastructure.client.dto.DeviceGroupsResponse;
import com.pulsegrid.alertingservice.infrastructure.client.dto.DeviceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class DeviceRegistryRestClientAdapter implements DeviceRegistryPort {

    private final RestClient deviceRegistryRestClient;

    @Override
    public Optional<DeviceSummary> findDevice(UUID deviceId) {
        return callOrEmptyOnNotFound(() -> deviceRegistryRestClient.get()
                .uri("/api/v1/internal/devices/{id}", deviceId)
                .retrieve()
                .body(DeviceResponse.class))
                .map(response -> new DeviceSummary(response.deviceId(), response.name()));
    }

    @Override
    public boolean groupExists(UUID groupId) {
        return callOrEmptyOnNotFound(() -> deviceRegistryRestClient.get()
                .uri("/api/v1/internal/device-groups/{id}", groupId)
                .retrieve()
                .toBodilessEntity())
                .isPresent();
    }

    @Override
    public List<UUID> findGroupIds(UUID deviceId) {
        return callOrEmptyOnNotFound(() -> deviceRegistryRestClient.get()
                .uri("/api/v1/internal/devices/{id}/groups", deviceId)
                .retrieve()
                .body(DeviceGroupsResponse.class))
                .map(DeviceGroupsResponse::groupIds)
                .orElse(List.of());
    }

    private <T> Optional<T> callOrEmptyOnNotFound(Supplier<T> call) {
        try {
            return Optional.ofNullable(call.get());
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw new DeviceRegistryUnavailableException(ex);
        } catch (RestClientException ex) {
            throw new DeviceRegistryUnavailableException(ex);
        }
    }
}
