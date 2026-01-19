package com.hoops.auth.adapter.out.oauth.exception;

import com.hoops.common.exception.DomainException;

/**
 * 카카오 API 호출 실패 예외
 * 카카오 OAuth API 호출 중 오류가 발생했을 때 발생합니다
 */
public class KakaoApiException extends DomainException {

    private static final String ERROR_CODE = "KAKAO_API_ERROR";

    public KakaoApiException(String message) {
        super(ERROR_CODE, message);
    }

    public KakaoApiException(String message, Throwable cause) {
        super(ERROR_CODE, message);
    }
}
