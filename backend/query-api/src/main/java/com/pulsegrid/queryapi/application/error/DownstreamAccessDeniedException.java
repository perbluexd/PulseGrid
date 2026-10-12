package com.pulsegrid.queryapi.application.error;

public class DownstreamAccessDeniedException extends RuntimeException {

    private final String service;

    public DownstreamAccessDeniedException(String service, Throwable cause) {
        super("Acceso denegado por " + service, cause);
        this.service = service;
    }

    public String getService() {
        return service;
    }
}
