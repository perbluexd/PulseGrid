package com.pulsegrid.alertingservice.application.port.out;

import java.util.UUID;

public record DeviceSummary(UUID deviceId, String name) {
}
