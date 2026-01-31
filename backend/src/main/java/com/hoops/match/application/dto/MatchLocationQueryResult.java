package com.hoops.match.application.dto;

import java.util.List;

public record MatchLocationQueryResult(
        List<MatchWithDistance> matches,
        int totalCount,
        boolean hasMore
) {}
