package com.hoops.common.exception;

/**
 * 모든 비즈니스 예외의 추상 베이스 클래스
 * 도메인, 애플리케이션, 인프라 예외의 공통 부모 클래스
 */
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;

    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
