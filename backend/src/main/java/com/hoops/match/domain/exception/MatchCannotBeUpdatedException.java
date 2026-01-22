package com.hoops.match.domain.exception;

import com.hoops.common.exception.DomainException;

public class MatchCannotBeUpdatedException extends DomainException {

    private static final String ERROR_CODE = "MATCH_CANNOT_BE_UPDATED";

    public MatchCannotBeUpdatedException(Long matchId, String status) {
        super(ERROR_CODE,
                String.format("Cannot update match in current status. (matchId: %d, status: %s)", matchId, status));
    }
}
