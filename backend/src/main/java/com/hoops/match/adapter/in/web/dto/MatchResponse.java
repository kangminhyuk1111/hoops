package com.hoops.match.adapter.in.web.dto;

import com.hoops.match.domain.model.Match;
import java.math.BigDecimal;

public record MatchResponse(
        Long id,
        Long hostId,
        String hostNickname,
        String title,
        String description,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        String matchDate,
        String startTime,
        String endTime,
        Integer maxParticipants,
        Integer currentParticipants,
        String status,
        Integer remainingSlots,
        String recruitmentStatus,
        Double distanceKm
) {

    public static MatchResponse of(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getHostId(),
                match.getHostNickname(),
                match.getTitle(),
                match.getDescription(),
                match.getLatitude(),
                match.getLongitude(),
                match.getAddress(),
                match.getMatchDate().toString(),
                match.getStartTime().toString(),
                match.getEndTime().toString(),
                match.getMaxParticipants(),
                match.getCurrentParticipants(),
                match.getStatus().name(),
                match.getRemainingSlots(),
                match.getRecruitmentStatus().name(),
                null
        );
    }

    public static MatchResponse of(Match match, Double distanceKm) {
        return new MatchResponse(
                match.getId(),
                match.getHostId(),
                match.getHostNickname(),
                match.getTitle(),
                match.getDescription(),
                match.getLatitude(),
                match.getLongitude(),
                match.getAddress(),
                match.getMatchDate().toString(),
                match.getStartTime().toString(),
                match.getEndTime().toString(),
                match.getMaxParticipants(),
                match.getCurrentParticipants(),
                match.getStatus().name(),
                match.getRemainingSlots(),
                match.getRecruitmentStatus().name(),
                distanceKm
        );
    }
}
