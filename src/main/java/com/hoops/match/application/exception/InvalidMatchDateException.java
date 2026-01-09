package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;
import java.time.LocalDate;

public class InvalidMatchDateException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_MATCH_DATE";

    public InvalidMatchDateException(LocalDate matchDate) {
        super(DEFAULT_ERROR_CODE,
                String.format("경기 날짜는 과거일 수 없습니다. (입력값: %s)", matchDate));
    }
}
