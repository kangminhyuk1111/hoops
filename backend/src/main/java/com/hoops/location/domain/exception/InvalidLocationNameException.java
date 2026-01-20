package com.hoops.location.domain.exception;

import com.hoops.common.exception.DomainException;

/**
 * 유효하지 않은 장소명 예외
 * 장소명이 비즈니스 규칙을 위반할 때 발생합니다
 */
public class InvalidLocationNameException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_LOCATION_NAME";

    public InvalidLocationNameException(String name) {
        super(DEFAULT_ERROR_CODE,
                String.format("장소명은 2자 이상이어야 합니다. (입력값: %s)", name));
    }
}
