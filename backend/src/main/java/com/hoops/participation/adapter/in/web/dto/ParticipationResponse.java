package com.hoops.participation.adapter.in.web.dto;

import com.hoops.participation.domain.model.Participation;
import java.time.LocalDateTime;

/**
 * 참가 응답 DTO
 */
public record ParticipationResponse(
        Long id,
        Long matchId,
        Long userId,
        String status,
        LocalDateTime joinedAt
) {
    public static ParticipationResponse of(Participation participation) {
        return new ParticipationResponse(
                participation.getId(),
                participation.getMatchId(),
                participation.getUserId(),
                participation.getStatus().name(),
                participation.getJoinedAt()
        );
    }
}
