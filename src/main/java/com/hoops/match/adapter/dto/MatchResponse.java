package com.hoops.match.adapter.dto;

import com.hoops.match.domain.Match;
import java.math.BigDecimal;

/**
 * 경기 조회 응답 DTO
 *
 * Domain Model인 Match를 HTTP 응답으로 변환합니다.
 * Java 17의 record를 사용하여 불변 객체로 구현합니다.
 */
public record MatchResponse(
        Long id,
        Long hostId,
        String hostNickname,
        String title,
        String description,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        String matchDate,
        String startTime,
        String endTime,
        Integer maxParticipants,
        Integer currentParticipants,
        String status
) {

    /**
     * Domain Match 객체를 MatchResponse DTO로 변환합니다.
     *
     * @param match 도메인 Match 객체
     * @return MatchResponse DTO
     */
    public static MatchResponse of(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getHostId(),
                match.getHostNickname(),
                match.getTitle(),
                match.getDescription(),
                match.getLatitude(),
                match.getLongitude(),
                match.getAddress(),
                match.getMatchDate().toString(),
                match.getStartTime().toString(),
                match.getEndTime().toString(),
                match.getMaxParticipants(),
                match.getCurrentParticipants(),
                match.getStatus().name()
        );
    }
}
