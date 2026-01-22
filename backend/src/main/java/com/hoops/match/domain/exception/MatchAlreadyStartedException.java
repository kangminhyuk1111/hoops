package com.hoops.match.domain.exception;

import com.hoops.common.exception.DomainException;

public class MatchAlreadyStartedException extends DomainException {

    private static final String ERROR_CODE = "INVALID_MATCH_STATUS";

    public MatchAlreadyStartedException(Long matchId) {
        super(ERROR_CODE,
                String.format("Cannot cancel a match that has already started. (matchId: %d)", matchId));
    }
}
