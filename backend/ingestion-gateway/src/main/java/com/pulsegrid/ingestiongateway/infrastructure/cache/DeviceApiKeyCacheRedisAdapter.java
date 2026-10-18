package com.pulsegrid.ingestiongateway.infrastructure.cache;

import com.pulsegrid.ingestiongateway.application.port.out.DeviceApiKeyCachePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class DeviceApiKeyCacheRedisAdapter implements DeviceApiKeyCachePort {

    private static final String CACHE_KEY_PREFIX = "device-api-key:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final Duration ttl;

    public DeviceApiKeyCacheRedisAdapter(ReactiveStringRedisTemplate redisTemplate,
                                         @Value("${app.cache.device-api-key-ttl}") Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
    }

    @Override
    public Mono<UUID> get(String apiKey) {
        return redisTemplate.opsForValue()
                .get(cacheKey(apiKey))
                .map(UUID::fromString);
    }

    @Override
    public Mono<Void> put(String apiKey, UUID deviceId) {
        return redisTemplate.opsForValue()
                .set(cacheKey(apiKey), deviceId.toString(), ttl)
                .then();
    }

    @Override
    public Mono<Void> evict(String apiKeyHash) {
        return redisTemplate.opsForValue()
                .delete(cacheKeyFromHash(apiKeyHash))
                .then();
    }

    private String cacheKey(String apiKey) {
        return cacheKeyFromHash(hash(apiKey));
    }

    private String cacheKeyFromHash(String apiKeyHash) {
        return CACHE_KEY_PREFIX + apiKeyHash;
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
