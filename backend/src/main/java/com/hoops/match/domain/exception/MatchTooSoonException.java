package com.hoops.match.domain.exception;

import com.hoops.common.exception.DomainException;

import java.time.LocalDateTime;

public class MatchTooSoonException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "MATCH_TOO_SOON";

    public MatchTooSoonException(LocalDateTime matchStartTime) {
        super(DEFAULT_ERROR_CODE,
                String.format("경기는 최소 1시간 후에 시작해야 합니다. (입력값: %s)", matchStartTime));
    }
}
