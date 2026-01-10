package com.hoops.user.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 유효하지 않은 임시 토큰 예외
 * 회원가입 완료 시 임시 토큰이 만료되었거나 유효하지 않을 때 발생합니다
 */
public class InvalidTempTokenException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_TEMP_TOKEN";

    public InvalidTempTokenException() {
        super(DEFAULT_ERROR_CODE, "만료되거나 유효하지 않은 임시 토큰입니다");
    }

    public InvalidTempTokenException(String reason) {
        super(DEFAULT_ERROR_CODE,
                String.format("유효하지 않은 임시 토큰입니다. (사유: %s)", reason));
    }
}
