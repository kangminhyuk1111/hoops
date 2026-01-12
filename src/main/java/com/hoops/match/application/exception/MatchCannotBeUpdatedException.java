package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 경기를 수정할 수 없는 상태일 때 발생하는 예외
 */
public class MatchCannotBeUpdatedException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "MATCH_CANNOT_BE_UPDATED";

    public MatchCannotBeUpdatedException(Long matchId, String status) {
        super(DEFAULT_ERROR_CODE,
                String.format("경기를 수정할 수 없는 상태입니다. (matchId: %d, status: %s)", matchId, status));
    }
}
