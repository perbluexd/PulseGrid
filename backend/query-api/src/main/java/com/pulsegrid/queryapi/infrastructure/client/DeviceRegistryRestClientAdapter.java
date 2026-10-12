package com.pulsegrid.queryapi.infrastructure.client;

import com.pulsegrid.queryapi.application.model.DeviceGroupView;
import com.pulsegrid.queryapi.application.model.DeviceStatus;
import com.pulsegrid.queryapi.application.model.DeviceView;
import com.pulsegrid.queryapi.application.port.out.DeviceRegistryPort;
import com.pulsegrid.queryapi.infrastructure.client.dto.DeviceGroupListResponse;
import com.pulsegrid.queryapi.infrastructure.client.dto.DeviceGroupResponse;
import com.pulsegrid.queryapi.infrastructure.client.dto.DeviceListResponse;
import com.pulsegrid.queryapi.infrastructure.client.dto.DeviceResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DeviceRegistryRestClientAdapter implements DeviceRegistryPort {

    private static final String SERVICE = "device-registry";

    private final RestClient restClient;

    public DeviceRegistryRestClientAdapter(@Qualifier("deviceRegistryRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<DeviceView> findDevice(UUID deviceId) {
        return DownstreamCall.executeOrEmptyOnNotFound(SERVICE, () -> restClient.get()
                        .uri("/api/v1/devices/{id}", deviceId)
                        .retrieve()
                        .body(DeviceResponse.class))
                .map(this::toView);
    }

    @Override
    public List<DeviceView> findAllDevices() {
        var response = DownstreamCall.execute(SERVICE, () -> restClient.get()
                .uri("/api/v1/devices")
                .retrieve()
                .body(DeviceListResponse.class));
        return response.devices().stream().map(this::toView).toList();
    }

    @Override
    public List<DeviceGroupView> findAllGroups() {
        var response = DownstreamCall.execute(SERVICE, () -> restClient.get()
                .uri("/api/v1/device-groups")
                .retrieve()
                .body(DeviceGroupListResponse.class));
        return response.groups().stream().map(this::toView).toList();
    }

    private DeviceView toView(DeviceResponse device) {
        return new DeviceView(device.id(), device.name(), device.type(), DeviceStatus.valueOf(device.status()),
                device.location(), device.createdAt(), device.lastSeenAt());
    }

    private DeviceGroupView toView(DeviceGroupResponse group) {
        return new DeviceGroupView(group.id(), group.name(), group.description());
    }
}
