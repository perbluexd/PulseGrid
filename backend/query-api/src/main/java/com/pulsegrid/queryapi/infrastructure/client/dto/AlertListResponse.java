package com.pulsegrid.queryapi.infrastructure.client.dto;

import java.util.List;

public record AlertListResponse(List<AlertResponse> alerts) {
}
