package com.hoops.location.application.port.in;

import java.math.BigDecimal;

/**
 * 장소 생성 커맨드
 *
 * 장소 생성에 필요한 데이터를 전달합니다.
 */
public record CreateLocationCommand(
        String name,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String description
) {
}
