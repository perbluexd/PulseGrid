package com.pulsegrid.queryapi.infrastructure.client;

import com.pulsegrid.queryapi.application.error.DownstreamAccessDeniedException;
import com.pulsegrid.queryapi.application.error.DownstreamServiceUnavailableException;
import com.pulsegrid.queryapi.application.error.InvalidQueryArgumentException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Optional;
import java.util.function.Supplier;

final class DownstreamCall {

    private DownstreamCall() {
    }

    static <T> T execute(String service, Supplier<T> call) {
        try {
            return call.get();
        } catch (HttpClientErrorException ex) {
            throw translate(service, ex);
        } catch (RestClientException ex) {
            throw new DownstreamServiceUnavailableException(service, ex);
        }
    }

    static <T> Optional<T> executeOrEmptyOnNotFound(String service, Supplier<T> call) {
        try {
            return Optional.ofNullable(call.get());
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw translate(service, ex);
        } catch (RestClientException ex) {
            throw new DownstreamServiceUnavailableException(service, ex);
        }
    }

    private static RuntimeException translate(String service, HttpClientErrorException ex) {
        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED || ex.getStatusCode() == HttpStatus.FORBIDDEN) {
            return new DownstreamAccessDeniedException(service, ex);
        }
        if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return new InvalidQueryArgumentException("Parámetros rechazados por " + service);
        }
        return new DownstreamServiceUnavailableException(service, ex);
    }
}
