package com.pulsegrid.deviceregistry.infrastructure.security;

import com.pulsegrid.deviceregistry.application.port.out.ApiKeyGeneratorPort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class ApiKeyGeneratorAdapter implements ApiKeyGeneratorPort {
    private static final int RAW_KEY_BYTES = 32;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateRawKey(){
        byte[] randomBytes = new byte[RAW_KEY_BYTES];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Override
    public String hash(String rawKey){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
