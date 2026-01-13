package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 호스트 조회 실패 예외
 *
 * 경기 생성 시 호스트 정보를 찾을 수 없을 때 발생합니다.
 */
public class HostNotFoundException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "HOST_NOT_FOUND";

    public HostNotFoundException(Long hostId) {
        super(DEFAULT_ERROR_CODE,
                String.format("호스트를 찾을 수 없습니다. (hostId: %d)", hostId));
    }
}
