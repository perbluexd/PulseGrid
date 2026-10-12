package com.pulsegrid.aggregationservice.api.dto;

import java.util.List;

public record LatestReadingsResponse(List<LatestReadingResponse> readings) {
}
