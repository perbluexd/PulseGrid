package com.pulsegrid.alertingservice.api.dto.alert;

import java.util.List;

public record ListAlertsResponse(List<AlertResponse> alerts) {
}
