package com.hoops.auth.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 유효하지 않은 리프레시 토큰 예외
 * 리프레시 토큰이 만료되었거나 유효하지 않을 때 발생합니다
 */
public class InvalidRefreshTokenException extends DomainException {

    private static final String ERROR_CODE = "INVALID_REFRESH_TOKEN";

    public InvalidRefreshTokenException() {
        super(ERROR_CODE, "유효하지 않거나 만료된 리프레시 토큰입니다");
    }

    public InvalidRefreshTokenException(String reason) {
        super(ERROR_CODE,
                String.format("유효하지 않은 리프레시 토큰입니다. (사유: %s)", reason));
    }
}
