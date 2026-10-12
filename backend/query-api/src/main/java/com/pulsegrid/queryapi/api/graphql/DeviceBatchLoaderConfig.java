package com.pulsegrid.queryapi.api.graphql;

import com.pulsegrid.queryapi.application.model.DeviceView;
import com.pulsegrid.queryapi.application.port.in.DeviceQueryUseCase;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
public class DeviceBatchLoaderConfig {

    public DeviceBatchLoaderConfig(BatchLoaderRegistry registry, DeviceQueryUseCase deviceQueryUseCase) {
        registry.forTypePair(UUID.class, DeviceView.class)
                .registerMappedBatchLoader((deviceIds, env) -> Mono.fromCallable(
                        () -> deviceQueryUseCase.getDevicesByIds(deviceIds)));
    }
}
