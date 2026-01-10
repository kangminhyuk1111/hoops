package com.hoops.participation.application.port.in;

/**
 * 경기 참가 커맨드
 *
 * 경기 참가에 필요한 데이터를 전달합니다.
 */
public record ParticipateInMatchCommand(
        Long matchId,
        Long userId
) {
}
