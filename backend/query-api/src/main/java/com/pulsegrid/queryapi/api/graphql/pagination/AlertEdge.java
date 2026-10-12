package com.pulsegrid.queryapi.api.graphql.pagination;

import com.pulsegrid.queryapi.application.model.AlertView;

public record AlertEdge(String cursor, AlertView node) {
}
