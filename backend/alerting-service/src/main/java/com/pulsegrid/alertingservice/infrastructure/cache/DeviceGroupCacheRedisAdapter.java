package com.pulsegrid.alertingservice.infrastructure.cache;

import com.pulsegrid.alertingservice.application.port.out.DeviceGroupCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DeviceGroupCacheRedisAdapter implements DeviceGroupCachePort {

    private static final String CACHE_KEY_PREFIX = "device-groups:";
    private static final String SEPARATOR = ",";
    private static final Duration TTL = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;

    @Override
    public Optional<List<UUID>> get(UUID deviceId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(cacheKey(deviceId)))
                .map(this::parse);
    }

    @Override
    public void put(UUID deviceId, List<UUID> groupIds) {
        String value = groupIds.stream().map(UUID::toString).collect(Collectors.joining(SEPARATOR));
        redisTemplate.opsForValue().set(cacheKey(deviceId), value, TTL);
    }

    @Override
    public void evict(UUID deviceId) {
        redisTemplate.delete(cacheKey(deviceId));
    }

    private List<UUID> parse(String value) {
        if (value.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(value.split(SEPARATOR)).map(UUID::fromString).toList();
    }

    private String cacheKey(UUID deviceId) {
        return CACHE_KEY_PREFIX + deviceId;
    }
}
