package com.hoops.match.adapter.in.web.dto;

import java.util.List;

public record MatchListResponse(
        List<MatchResponse> items,
        int totalCount,
        boolean hasMore
) {}
