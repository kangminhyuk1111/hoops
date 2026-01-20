package com.hoops.match.application.port.out;

import com.hoops.match.application.dto.LocationInfo;

/**
 * 장소 정보 제공 포트
 *
 * Match 도메인에서 장소 정보를 조회하기 위한 아웃바운드 포트입니다.
 * Location 도메인에 직접 의존하지 않고 이 인터페이스를 통해 필요한 정보만 조회합니다.
 *
 * @see LocationInfo
 */
public interface LocationInfoPort {

    /**
     * 장소 정보를 조회합니다.
     *
     * @param locationId 장소 ID
     * @return 장소 정보
     * @throws com.hoops.match.application.exception.MatchLocationNotFoundException 장소를 찾을 수 없는 경우
     */
    LocationInfo getLocationInfo(Long locationId);
}
