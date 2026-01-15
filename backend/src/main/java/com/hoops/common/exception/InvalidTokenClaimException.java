package com.hoops.common.exception;

/**
 * JWT 토큰의 클레임이 유효하지 않을 때 발생하는 예외
 */
public class InvalidTokenClaimException extends DomainException {

    private static final String ERROR_CODE = "INVALID_TOKEN_CLAIM";

    public InvalidTokenClaimException(String message) {
        super(ERROR_CODE, message);
    }

    public InvalidTokenClaimException() {
        super(ERROR_CODE, "토큰의 클레임이 유효하지 않습니다");
    }
}
