package com.hoops.participation.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 유효하지 않은 경기 상태 예외
 * 참가 불가능한 상태의 경기에 참가 신청할 때 발생합니다
 */
public class InvalidMatchStatusException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_MATCH_STATUS";

    public InvalidMatchStatusException(Long matchId, String status) {
        super(DEFAULT_ERROR_CODE,
                String.format("참가 신청할 수 없는 경기 상태입니다. (경기 ID: %d, 상태: %s)", matchId, status));
    }
}
