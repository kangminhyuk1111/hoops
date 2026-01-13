package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;

public class CancelTimeExceededException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "CANCEL_TIME_EXCEEDED";

    public CancelTimeExceededException(Long matchId) {
        super(DEFAULT_ERROR_CODE,
                String.format("경기 시작 2시간 전에는 취소할 수 없습니다. (경기 ID: %d)", matchId));
    }
}
