package com.hoops.participation.application.port.in;

/**
 * 경기 참가 승인 커맨드
 */
public record ApproveParticipationCommand(
        Long matchId,
        Long participationId,
        Long hostUserId
) {
}
