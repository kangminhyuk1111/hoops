package com.hoops.participation.application.port.in;

/**
 * 경기 참가 거절 커맨드
 */
public record RejectParticipationCommand(
        Long matchId,
        Long participationId,
        Long hostUserId
) {
}
