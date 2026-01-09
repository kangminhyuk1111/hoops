package com.hoops.common.exception;

import com.hoops.common.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 * BusinessException 계층의 모든 예외를 처리한다
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * DomainException 처리
     * 도메인 규칙 위반 시 발생하는 예외를 처리한다
     * HTTP Status: 400 BAD_REQUEST 또는 404 NOT_FOUND
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException exception) {
        logger.warn("Domain exception occurred: {} - {}", exception.getErrorCode(), exception.getMessage());

        HttpStatus status = determineHttpStatus(exception);
        ErrorResponse errorResponse = ErrorResponse.of(
                exception.getErrorCode(),
                exception.getMessage()
        );

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * BusinessException 처리 (DomainException을 제외한 나머지)
     * ApplicationException, InfrastructureException 등을 처리한다
     * HTTP Status: 500 INTERNAL_SERVER_ERROR 또는 503 SERVICE_UNAVAILABLE
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        logger.error("Business exception occurred: {} - {}", exception.getErrorCode(), exception.getMessage(), exception);

        HttpStatus status = determineHttpStatus(exception);
        ErrorResponse errorResponse = ErrorResponse.of(
                exception.getErrorCode(),
                exception.getMessage()
        );

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * 예상하지 못한 예외 처리
     * HTTP Status: 500 INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        logger.error("Unexpected exception occurred", exception);

        ErrorResponse errorResponse = ErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "예상하지 못한 오류가 발생했습니다."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 예외 타입과 에러 코드에 따라 HTTP Status를 결정한다
     */
    private HttpStatus determineHttpStatus(BusinessException exception) {
        String errorCode = exception.getErrorCode();

        // NOT_FOUND 계열
        if (errorCode.contains("NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }

        // DUPLICATE 계열 또는 INVALID 계열
        if (errorCode.contains("DUPLICATE") || errorCode.contains("INVALID")) {
            return HttpStatus.BAD_REQUEST;
        }

        // DomainException의 기본값
        if (exception instanceof DomainException) {
            return HttpStatus.BAD_REQUEST;
        }

        // InfrastructureException (존재할 경우)
        if (exception.getClass().getSimpleName().contains("Infrastructure")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        // 기본값
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
