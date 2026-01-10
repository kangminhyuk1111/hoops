package com.hoops.match.application.port.in;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 경기 생성 커맨드
 *
 * 경기 생성에 필요한 데이터를 전달합니다.
 * hostId는 인증된 사용자의 ID입니다.
 */
public record CreateMatchCommand(
        Long hostId,
        Long locationId,
        String title,
        String description,
        LocalDate matchDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer maxParticipants
) {
}
