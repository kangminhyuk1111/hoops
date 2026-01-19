package com.hoops.auth.adapter.out.oauth.exception;

import com.hoops.common.exception.DomainException;

/**
 * 유효하지 않은 인가 코드 예외
 * OAuth 인가 코드가 만료되었거나 유효하지 않을 때 발생합니다
 */
public class InvalidAuthCodeException extends DomainException {

    private static final String ERROR_CODE = "INVALID_AUTH_CODE";

    public InvalidAuthCodeException() {
        super(ERROR_CODE, "유효하지 않은 인가 코드입니다");
    }

    public InvalidAuthCodeException(String reason) {
        super(ERROR_CODE,
                String.format("유효하지 않은 인가 코드입니다. (사유: %s)", reason));
    }
}
