package com.pulsegrid.ingestiongateway.infrastructure.cache;

import com.pulsegrid.ingestiongateway.application.port.out.DeviceApiKeyCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeviceApiKeyCacheRedisAdapter implements DeviceApiKeyCachePort {

    private static final String CACHE_KEY_PREFIX = "device-api-key:";

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<UUID> get(String apiKey) {
        return redisTemplate.opsForValue()
                .get(cacheKey(apiKey))
                .map(UUID::fromString);
    }

    @Override
    public Mono<Void> put(String apiKey, UUID deviceId) {
        return redisTemplate.opsForValue()
                .set(cacheKey(apiKey), deviceId.toString())
                .then();
    }

    private String cacheKey(String apiKey) {
        return CACHE_KEY_PREFIX + hash(apiKey);
    }

    private String hash(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
