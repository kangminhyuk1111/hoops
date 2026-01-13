package com.hoops.location.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 중복된 장소명 예외
 * 이미 존재하는 장소명으로 등록을 시도할 때 발생합니다
 */
public class DuplicateLocationNameException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "DUPLICATE_LOCATION_NAME";

    public DuplicateLocationNameException(String name) {
        super(DEFAULT_ERROR_CODE,
                String.format("이미 존재하는 장소명입니다. (장소명: %s)", name));
    }
}
