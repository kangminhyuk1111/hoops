package com.hoops.user.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 유효하지 않은 카카오 인가코드 예외
 * 카카오 OAuth 인증 과정에서 인가코드가 유효하지 않을 때 발생합니다
 */
public class InvalidAuthCodeException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_AUTH_CODE";

    public InvalidAuthCodeException() {
        super(DEFAULT_ERROR_CODE, "유효하지 않은 인가코드입니다");
    }

    public InvalidAuthCodeException(String reason) {
        super(DEFAULT_ERROR_CODE,
                String.format("유효하지 않은 인가코드입니다. (사유: %s)", reason));
    }
}
