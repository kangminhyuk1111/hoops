package com.hoops.location.application.port.in;

import com.hoops.location.domain.model.Location;

import java.util.List;

/**
 * 장소 조회 유스케이스
 */
public interface LocationQueryUseCase {

    /**
     * 전체 장소 목록을 조회합니다.
     *
     * @return 장소 목록
     */
    List<Location> getAllLocations();

    /**
     * 키워드로 장소를 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색 결과 장소 목록
     */
    List<Location> searchLocations(String keyword);

    /**
     * 장소 상세 정보를 조회합니다.
     *
     * @param locationId 장소 ID
     * @return 장소 정보
     * @throws com.hoops.location.application.exception.LocationNotFoundException 장소가 존재하지 않을 경우
     */
    Location getLocationById(Long locationId);
}
