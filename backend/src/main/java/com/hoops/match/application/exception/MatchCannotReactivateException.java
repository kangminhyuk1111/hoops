package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;

public class MatchCannotReactivateException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "MATCH_CANNOT_REACTIVATE";

    public MatchCannotReactivateException(Long matchId, String reason) {
        super(DEFAULT_ERROR_CODE,
                String.format("경기를 복구할 수 없습니다. (matchId: %d, 사유: %s)", matchId, reason));
    }
}
