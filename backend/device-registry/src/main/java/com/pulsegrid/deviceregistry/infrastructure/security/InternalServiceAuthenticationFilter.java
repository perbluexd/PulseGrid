package com.pulsegrid.deviceregistry.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";

    private final InternalServiceTokenProvider internalServiceTokenProvider;

    public InternalServiceAuthenticationFilter(InternalServiceTokenProvider internalServiceTokenProvider) {
        this.internalServiceTokenProvider = internalServiceTokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(INTERNAL_SERVICE_HEADER);

        if (header != null && isValidToken(header)) {
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"));
            var authentication = new UsernamePasswordAuthenticationToken("internal-service", null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidToken(String candidate) {
        byte[] candidateBytes = candidate.getBytes(StandardCharsets.UTF_8);
        byte[] expectedBytes = internalServiceTokenProvider.getToken().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(candidateBytes, expectedBytes);
    }
}
