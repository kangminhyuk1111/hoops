package com.hoops.match.application.dto;

import java.math.BigDecimal;

/**
 * 경기 장소 정보
 *
 * Match 도메인에서 필요한 장소 정보만 정의합니다.
 * Location 도메인에 직접 의존하지 않고 필요한 정보만 전달받습니다.
 */
public record LocationInfo(
        Long locationId,
        BigDecimal latitude,
        BigDecimal longitude,
        String address
) {
}
