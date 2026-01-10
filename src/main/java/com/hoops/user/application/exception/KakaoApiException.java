package com.hoops.user.application.exception;

import com.hoops.common.exception.BusinessException;

/**
 * 카카오 API 호출 실패 예외
 * 카카오 OAuth API 호출 중 오류가 발생할 때 발생합니다
 * Infrastructure 계열 예외로 502 Bad Gateway를 반환합니다
 */
public class KakaoApiException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "KAKAO_API_ERROR";

    public KakaoApiException() {
        super(DEFAULT_ERROR_CODE, "카카오 API 호출에 실패했습니다");
    }

    public KakaoApiException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    public KakaoApiException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }
}
