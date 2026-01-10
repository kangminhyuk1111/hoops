package com.hoops.location.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 외부 Context에 제공하는 장소 조회 포트
 *
 * Location Context 외부에서 장소 정보를 조회할 때 사용합니다.
 * 내부 Repository를 직접 노출하지 않고, 필요한 정보만 제공합니다.
 */
public interface LocationQueryPort {

    /**
     * 장소 정보를 조회합니다.
     *
     * @param locationId 장소 ID
     * @return 장소 정보 (장소가 없으면 empty)
     */
    Optional<LocationData> findById(Long locationId);

    /**
     * 외부에 제공하는 장소 정보
     */
    record LocationData(
            Long id,
            BigDecimal latitude,
            BigDecimal longitude,
            String address
    ) {
    }
}
