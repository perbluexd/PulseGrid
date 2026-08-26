package com.pulsegrid.deviceregistry.infrastructure.security;

import com.pulsegrid.deviceregistry.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasherAdapter implements PasswordHasherPort {
    private final Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    @Override
    public String hash(String rawPassword){
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hash){
        return encoder.matches(rawPassword, hash);
    }
}
