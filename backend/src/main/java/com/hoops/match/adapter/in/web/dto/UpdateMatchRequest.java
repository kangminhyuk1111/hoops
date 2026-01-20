package com.hoops.match.adapter.in.web.dto;

import com.hoops.match.application.port.in.UpdateMatchCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 경기 수정 요청 DTO
 *
 * HTTP 요청을 UpdateMatchCommand로 변환합니다.
 * 모든 필드는 선택적이며, null인 필드는 수정되지 않습니다.
 */
public record UpdateMatchRequest(
        @Size(min = 1, max = 200, message = "경기 제목은 1자 이상 200자 이하여야 합니다")
        String title,

        @Size(max = 5000, message = "경기 설명은 5000자를 초과할 수 없습니다")
        String description,

        LocalDate matchDate,

        LocalTime startTime,

        LocalTime endTime,

        @Min(value = 4, message = "최대 참가 인원은 최소 4명 이상이어야 합니다")
        Integer maxParticipants
) {

    /**
     * UpdateMatchRequest를 UpdateMatchCommand로 변환합니다.
     *
     * @param matchId 경기 ID
     * @param userId JWT Token에서 추출한 사용자 ID
     * @return UpdateMatchCommand
     */
    public UpdateMatchCommand toCommand(Long matchId, Long userId) {
        return new UpdateMatchCommand(
                matchId,
                userId,
                title,
                description,
                matchDate,
                startTime,
                endTime,
                maxParticipants
        );
    }
}
