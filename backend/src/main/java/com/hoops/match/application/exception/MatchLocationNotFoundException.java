package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 경기 장소 조회 실패 예외
 *
 * 경기 생성 시 장소 정보를 찾을 수 없을 때 발생합니다.
 */
public class MatchLocationNotFoundException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "MATCH_LOCATION_NOT_FOUND";

    public MatchLocationNotFoundException(Long locationId) {
        super(DEFAULT_ERROR_CODE,
                String.format("장소를 찾을 수 없습니다. (locationId: %d)", locationId));
    }
}
