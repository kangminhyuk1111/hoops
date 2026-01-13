package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;

import java.time.LocalDate;

public class MatchTooFarException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "MATCH_TOO_FAR";

    public MatchTooFarException(LocalDate matchDate, int maxDays) {
        super(DEFAULT_ERROR_CODE,
                String.format("경기는 %d일 이내에만 생성할 수 있습니다. (입력값: %s)", maxDays, matchDate));
    }
}
