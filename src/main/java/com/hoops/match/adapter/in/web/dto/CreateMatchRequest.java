package com.hoops.match.adapter.in.web.dto;

import com.hoops.match.application.port.in.CreateMatchCommand;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 경기 생성 요청 DTO
 *
 * HTTP 요청을 CreateMatchCommand로 변환합니다.
 * Java 17의 record를 사용하여 불변 객체로 구현합니다.
 */
public record CreateMatchRequest(
        Long hostId,
        String title,
        String description,
        Long locationId,
        LocalDate matchDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer maxParticipants
) {

    /**
     * CreateMatchRequest를 CreateMatchCommand로 변환합니다.
     *
     * @return CreateMatchCommand
     */
    public CreateMatchCommand toCommand() {
        return new CreateMatchCommand(
                hostId,
                title,
                description,
                locationId,
                matchDate,
                startTime,
                endTime,
                maxParticipants
        );
    }
}
