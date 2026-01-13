package com.hoops.location.adapter.in.web.dto;

import com.hoops.location.domain.Location;
import java.math.BigDecimal;

/**
 * 장소 조회 응답 DTO
 *
 * Domain Model인 Location을 HTTP 응답으로 변환합니다.
 * Java 17의 record를 사용하여 불변 객체로 구현합니다.
 */
public record LocationResponse(
        Long id,
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {

    /**
     * Domain Location 객체를 LocationResponse DTO로 변환합니다.
     *
     * @param location 도메인 Location 객체
     * @return LocationResponse DTO
     */
    public static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getAlias(),
                location.getAddress(),
                location.getLatitude(),
                location.getLongitude()
        );
    }
}
