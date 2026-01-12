package com.hoops.common.exception;

/**
 * 도메인 규칙 위반 시 발생하는 예외의 베이스 클래스
 * 비즈니스 로직 검증 실패 시 사용
 */
public abstract class DomainException extends BusinessException {

    protected DomainException(String errorCode, String message) {
        super(errorCode, message);
    }

    protected DomainException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
