package com.hoops.match.adapter.dto;

import com.hoops.match.application.port.in.CreateMatchCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 경기 생성 요청 DTO
 *
 * HTTP 요청을 CreateMatchCommand로 변환합니다.
 * hostId는 JWT Token에서 추출하므로 Request에 포함하지 않습니다.
 */
public record CreateMatchRequest(
        @NotNull(message = "위치 정보는 필수입니다")
        Long locationId,

        @NotBlank(message = "경기 제목은 필수입니다")
        @Size(min = 1, max = 200, message = "경기 제목은 1자 이상 200자 이하여야 합니다")
        String title,

        @Size(max = 5000, message = "경기 설명은 5000자를 초과할 수 없습니다")
        String description,

        @NotNull(message = "경기 날짜는 필수입니다")
        LocalDate matchDate,

        @NotNull(message = "시작 시간은 필수입니다")
        LocalTime startTime,

        @NotNull(message = "종료 시간은 필수입니다")
        LocalTime endTime,

        @NotNull(message = "최대 참가 인원은 필수입니다")
        @Min(value = 4, message = "최대 참가 인원은 최소 4명 이상이어야 합니다")
        Integer maxParticipants
) {

    /**
     * CreateMatchRequest를 CreateMatchCommand로 변환합니다.
     *
     * @param hostId JWT Token에서 추출한 사용자 ID
     * @return CreateMatchCommand
     */
    public CreateMatchCommand toCommand(Long hostId) {
        return new CreateMatchCommand(
                hostId,
                locationId,
                title,
                description,
                matchDate,
                startTime,
                endTime,
                maxParticipants
        );
    }
}
