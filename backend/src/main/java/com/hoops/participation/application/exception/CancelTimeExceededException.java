package com.hoops.participation.application.exception;

import com.hoops.common.exception.ApplicationException;

public class CancelTimeExceededException extends ApplicationException {

    private static final String DEFAULT_ERROR_CODE = "CANCEL_TIME_EXCEEDED";

    public CancelTimeExceededException(Long participationId) {
        super(DEFAULT_ERROR_CODE,
                String.format("경기 시작 2시간 전에는 참가를 취소할 수 없습니다. (참가 ID: %d)", participationId));
    }
}
