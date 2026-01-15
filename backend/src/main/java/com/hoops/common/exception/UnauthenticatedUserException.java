package com.hoops.common.exception;

/**
 * 인증되지 않은 사용자가 인증이 필요한 작업을 시도할 때 발생하는 예외
 */
public class UnauthenticatedUserException extends DomainException {

    private static final String ERROR_CODE = "UNAUTHENTICATED_USER";

    public UnauthenticatedUserException(String message) {
        super(ERROR_CODE, message);
    }

    public UnauthenticatedUserException() {
        super(ERROR_CODE, "인증되지 않은 사용자입니다");
    }
}
