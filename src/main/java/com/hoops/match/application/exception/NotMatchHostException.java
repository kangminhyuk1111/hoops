package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;

public class NotMatchHostException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "NOT_HOST";

    public NotMatchHostException(Long matchId, Long userId) {
        super(DEFAULT_ERROR_CODE,
                String.format("경기 호스트만 취소할 수 있습니다. (matchId: %d, userId: %d)", matchId, userId));
    }
}
