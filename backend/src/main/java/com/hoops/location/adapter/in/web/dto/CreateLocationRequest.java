package com.hoops.location.adapter.in.web.dto;

import com.hoops.location.application.port.in.CreateLocationCommand;
import java.math.BigDecimal;

/**
 * 장소 생성 요청 DTO
 *
 * HTTP 요청을 CreateLocationCommand로 변환합니다.
 * Java 17의 record를 사용하여 불변 객체로 구현합니다.
 */
public record CreateLocationRequest(
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String description
) {

    /**
     * CreateLocationRequest를 CreateLocationCommand로 변환합니다.
     *
     * @return CreateLocationCommand
     */
    public CreateLocationCommand toCommand() {
        return new CreateLocationCommand(
                name,
                address,
                latitude,
                longitude,
                description
        );
    }
}
