package com.pulsegrid.deviceregistry.infrastructure.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
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
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtParser jwtParser;

    public JwtAuthenticationFilter(JwtKeyProvider jwtKeyProvider) {
        this.jwtParser = Jwts.parser()
                .verifyWith(jwtKeyProvider.getKey())
                .build();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION_HEADER);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                var claims = jwtParser.parseSignedClaims(token).getPayload();// var es igual Claims

                String userId = claims.getSubject();
                String role = claims.get("role", String.class);

                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));// var significa List<SimpleGrantedAuthority>
                var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);// var puede ser Authentication que es la interfaz
                // o ser directamente UsernamePasswordAuthenticationToken
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
