package com.pulsegrid.queryapi.application.error;

public class DownstreamServiceUnavailableException extends RuntimeException {

    private final String service;

    public DownstreamServiceUnavailableException(String service, Throwable cause) {
        super("El servicio " + service + " no está disponible", cause);
        this.service = service;
    }

    public String getService() {
        return service;
    }
}
