package com.hoops.match.application.port.in;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 경기 생성 커맨드
 * <p>
 * 경기 생성에 필요한 데이터를 전달합니다.
 */
public record CreateMatchCommand(
        Long hostId, String title, String description, Long locationId, LocalDate matchDate,
        LocalTime startTime, LocalTime endTime, Integer maxParticipants)
{
}
