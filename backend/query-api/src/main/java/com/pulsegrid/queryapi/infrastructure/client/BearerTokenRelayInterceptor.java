package com.pulsegrid.queryapi.infrastructure.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

public class BearerTokenRelayInterceptor implements ClientHttpRequestInterceptor {

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body,
                                        @NonNull ClientHttpRequestExecution execution) throws IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String token) {
            request.getHeaders().setBearerAuth(token);
        }
        return execution.execute(request, body);
    }
}
