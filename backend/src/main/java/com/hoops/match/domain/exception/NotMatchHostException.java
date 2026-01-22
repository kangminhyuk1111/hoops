package com.hoops.match.domain.exception;

import com.hoops.common.exception.DomainException;

public class NotMatchHostException extends DomainException {

    private static final String ERROR_CODE = "NOT_HOST";

    public NotMatchHostException(Long matchId, Long userId) {
        super(ERROR_CODE,
                String.format("Only the host can modify or cancel the match. (matchId: %d, userId: %d)", matchId, userId));
    }
}
