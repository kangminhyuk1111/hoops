package com.hoops.common.dto;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 * 클라이언트에게 전달되는 에러 정보를 담는다
 */
public record ErrorResponse(
        String errorCode,
        String message,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message, LocalDateTime.now());
    }
}
