package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;

public class CancelReasonRequiredException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "CANCEL_REASON_REQUIRED";

    public CancelReasonRequiredException(Long matchId) {
        super(DEFAULT_ERROR_CODE,
                String.format("경기 취소 시 사유를 입력해야 합니다. (경기 ID: %d)", matchId));
    }
}
