package com.hoops.match.application.port.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * 경기 참가 정보 제공 포트
 *
 * Match Context가 외부(Participation Context 등)에 제공하는 포트입니다.
 * 경기 참가와 관련된 정보 조회 및 참가자 추가 기능을 제공합니다.
 */
public interface MatchParticipationPort {

    /**
     * 참가 가능 여부 확인을 위한 경기 정보를 조회합니다.
     *
     * @param matchId 경기 ID
     * @return 경기 참가 정보
     */
    Optional<MatchParticipationData> findMatchForParticipation(Long matchId);

    /**
     * 경기에 참가자를 추가합니다.
     *
     * @param matchId 경기 ID
     */
    void addParticipant(Long matchId);

    /**
     * 경기에서 참가자를 제거합니다.
     *
     * @param matchId 경기 ID
     */
    void removeParticipant(Long matchId);

    /**
     * 경기 참가 정보 데이터
     */
    record MatchParticipationData(
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
}
