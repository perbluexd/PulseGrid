package com.pulsegrid.deviceregistry.application.port.out;

public interface ApiKeyGeneratorPort {
    String generateRawKey();
    String hash(String rawKey);
}
