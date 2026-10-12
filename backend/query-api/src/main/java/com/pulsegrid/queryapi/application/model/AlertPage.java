package com.pulsegrid.queryapi.application.model;

import java.util.List;

public record AlertPage(List<AlertView> alerts, int startOffset, boolean hasNextPage, int totalCount) {
}
