package com.hoops.common.exception;

/**
 * 애플리케이션(유스케이스) 실패 시 발생하는 예외의 베이스 클래스
 * 도메인 규칙이 아닌 애플리케이션 레벨의 비즈니스 로직 실패 시 사용
 */
public abstract class ApplicationException extends BusinessException {

    protected ApplicationException(String errorCode, String message) {
        super(errorCode, message);
    }

    protected ApplicationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
