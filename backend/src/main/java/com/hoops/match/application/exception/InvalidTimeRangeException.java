package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;
import java.time.LocalTime;

public class InvalidTimeRangeException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_TIME_RANGE";

    public InvalidTimeRangeException(LocalTime startTime, LocalTime endTime) {
        super(DEFAULT_ERROR_CODE,
                String.format("시작 시간은 종료 시간보다 빨라야 합니다. (시작: %s, 종료: %s)", startTime, endTime));
    }
}
