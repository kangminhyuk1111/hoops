package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;

public class MatchAlreadyStartedException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_MATCH_STATUS";

    public MatchAlreadyStartedException(Long matchId) {
        super(DEFAULT_ERROR_CODE,
                String.format("이미 시작된 경기는 취소할 수 없습니다. (matchId: %d)", matchId));
    }
}
