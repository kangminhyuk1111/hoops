package com.hoops.participation.application.port.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 경기 정보
 *
 * Participation Context에서 사용하는 경기 정보 DTO입니다.
 * Match Context의 내부 구현에 의존하지 않고 필요한 정보만 담습니다.
 */
public record MatchInfo(
        Long matchId,
        Long hostId,
        String title,
        String status,
        Integer currentParticipants,
        Integer maxParticipants,
        LocalDate matchDate,
        LocalTime startTime
) {
    public boolean isHost(Long userId) {
        return hostId.equals(userId);
    }

    public boolean canParticipate() {
        return ("PENDING".equals(status) || "CONFIRMED".equals(status))
                && currentParticipants < maxParticipants;
    }

    public boolean isFull() {
        return currentParticipants >= maxParticipants;
    }

    public boolean hasStarted() {
        LocalDateTime matchStartDateTime = LocalDateTime.of(matchDate, startTime);
        return LocalDateTime.now().isAfter(matchStartDateTime);
    }
}
