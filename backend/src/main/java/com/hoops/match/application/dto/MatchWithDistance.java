package com.hoops.match.application.dto;

import com.hoops.match.domain.model.Match;

public record MatchWithDistance(Match match, double distanceKm) {}
