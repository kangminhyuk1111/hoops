package com.hoops.participation.application.exception;

import com.hoops.common.exception.ApplicationException;

/**
 * 경기 정원 초과 예외
 * 경기 정원이 다 찼을 때 발생합니다
 */
public class MatchFullException extends ApplicationException {

    private static final String DEFAULT_ERROR_CODE = "MATCH_FULL";

    public MatchFullException(Long matchId) {
        super(DEFAULT_ERROR_CODE,
                String.format("경기 정원이 모두 찼습니다. (경기 ID: %d)", matchId));
    }
}
