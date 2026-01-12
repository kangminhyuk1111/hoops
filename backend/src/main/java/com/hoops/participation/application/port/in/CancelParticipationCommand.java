package com.hoops.participation.application.port.in;

/**
 * 경기 참가 취소 커맨드
 */
public record CancelParticipationCommand(
        Long matchId,
        Long participationId,
        Long userId
) {
}
