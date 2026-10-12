package com.pulsegrid.queryapi.api.graphql.pagination;

import java.util.List;

public record AlertConnection(List<AlertEdge> edges, PageInfo pageInfo, int totalCount) {
}
