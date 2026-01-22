package com.hoops.match.domain.exception;

import com.hoops.common.exception.DomainException;

public class MatchCannotReactivateException extends DomainException {

    private static final String ERROR_CODE = "MATCH_CANNOT_REACTIVATE";

    public MatchCannotReactivateException(Long matchId, String reason) {
        super(ERROR_CODE,
                String.format("Cannot reactivate match. (matchId: %d, reason: %s)", matchId, reason));
    }
}
