package com.hoops.match.application.port.in;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 경기 수정 커맨드
 *
 * 경기 수정에 필요한 데이터를 전달합니다.
 * userId는 인증된 사용자의 ID이며, 호스트 검증에 사용됩니다.
 */
public record UpdateMatchCommand(
        Long matchId,
        Long userId,
        String title,
        String description,
        LocalDate matchDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer maxParticipants
) {
}
