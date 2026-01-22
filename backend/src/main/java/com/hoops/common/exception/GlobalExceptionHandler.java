package com.hoops.common.exception;

import com.hoops.common.dto.ErrorResponse;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
     * Validation 예외 처리 (@Valid 실패)
     * HTTP Status: 400 BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException exception) {
        logger.warn("Validation exception occurred");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errorCode", "VALIDATION_FAILED");
        errorResponse.put("message", "입력값 검증에 실패했습니다");

        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        errorResponse.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

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
     * ApplicationException 처리
     * 유스케이스 실패 시 발생하는 예외를 처리한다
     * HTTP Status: 400 BAD_REQUEST, 404 NOT_FOUND, 409 CONFLICT 등
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException exception) {
        logger.warn("Application exception occurred: {} - {}", exception.getErrorCode(), exception.getMessage());

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
     * 낙관적 락 충돌 예외 처리
     * HTTP Status: 409 CONFLICT
     */
    @ExceptionHandler({OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(Exception exception) {
        logger.warn("Optimistic lock exception occurred: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                "CONCURRENT_MODIFICATION",
                "동시에 다른 사용자가 수정하여 충돌이 발생했습니다. 다시 시도해주세요."
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
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

        // NOT_FOUND 계열 → 404
        if (errorCode.contains("NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }

        // DUPLICATE, CONFLICT, ALREADY, OVERLAPPING 계열 → 409 Conflict
        if (errorCode.contains("DUPLICATE") || errorCode.contains("CONFLICT")
                || errorCode.contains("ALREADY") || errorCode.contains("OVERLAPPING")) {
            return HttpStatus.CONFLICT;
        }

        // 권한 없음 (본인이 아닌 경우) → 403 Forbidden
        if (errorCode.equals("NOT_PARTICIPANT") || errorCode.contains("NOT_HOST")) {
            return HttpStatus.FORBIDDEN;
        }

        // 인증 관련 예외 → 401 Unauthorized
        if (errorCode.contains("INVALID_REFRESH_TOKEN") || errorCode.contains("UNAUTHORIZED")) {
            return HttpStatus.UNAUTHORIZED;
        }

        // 외부 API 호출 실패 → 502 Bad Gateway
        if (errorCode.contains("KAKAO_API") || errorCode.contains("EXTERNAL_API")) {
            return HttpStatus.BAD_GATEWAY;
        }

        // INVALID 계열 → 400 Bad Request
        if (errorCode.contains("INVALID")) {
            return HttpStatus.BAD_REQUEST;
        }

        // DomainException의 기본값
        if (exception instanceof DomainException) {
            return HttpStatus.BAD_REQUEST;
        }

        // ApplicationException의 기본값
        if (exception instanceof ApplicationException) {
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
