package com.pulsegrid.queryapi.api.graphql.pagination;

public record PageInfo(boolean hasNextPage, String endCursor) {
}
