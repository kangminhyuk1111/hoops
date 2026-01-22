package com.hoops.match.domain.exception;

import com.hoops.common.exception.DomainException;

public class CancelTimeExceededException extends DomainException {

    private static final String ERROR_CODE = "CANCEL_TIME_EXCEEDED";

    public CancelTimeExceededException(Long matchId) {
        super(ERROR_CODE,
                String.format("Cannot cancel match within 2 hours of start. (matchId: %d)", matchId));
    }
}
