package com.hoops.match.domain.exception;

import com.hoops.common.exception.DomainException;

public class MatchCannotBeCancelledException extends DomainException {

    private static final String ERROR_CODE = "MATCH_CANNOT_BE_CANCELLED";

    public MatchCannotBeCancelledException(Long matchId, String reason) {
        super(ERROR_CODE,
                String.format("Cannot cancel match. (matchId: %d, reason: %s)", matchId, reason));
    }
}
