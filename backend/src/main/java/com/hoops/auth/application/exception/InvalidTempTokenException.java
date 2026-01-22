package com.hoops.auth.application.exception;

import com.hoops.common.exception.ApplicationException;

/**
 * 유효하지 않은 임시 토큰 예외
 * 회원가입 완료 시 임시 토큰이 만료되었거나 유효하지 않을 때 발생합니다
 */
public class InvalidTempTokenException extends ApplicationException {

    private static final String ERROR_CODE = "INVALID_TEMP_TOKEN";

    public InvalidTempTokenException() {
        super(ERROR_CODE, "만료되거나 유효하지 않은 임시 토큰입니다");
    }

    public InvalidTempTokenException(String reason) {
        super(ERROR_CODE,
                String.format("유효하지 않은 임시 토큰입니다. (사유: %s)", reason));
    }
}
