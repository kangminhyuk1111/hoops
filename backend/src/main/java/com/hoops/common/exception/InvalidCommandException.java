package com.hoops.common.exception;

/**
 * Command 객체의 유효성 검증 실패 시 발생하는 예외
 */
public class InvalidCommandException extends DomainException {

    private static final String ERROR_CODE = "INVALID_COMMAND";

    public InvalidCommandException(String message) {
        super(ERROR_CODE, message);
    }

    public InvalidCommandException(String field, String reason) {
        super(ERROR_CODE, field + ": " + reason);
    }
}
