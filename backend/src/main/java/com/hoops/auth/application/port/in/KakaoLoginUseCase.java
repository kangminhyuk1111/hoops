package com.hoops.auth.application.port.in;

import com.hoops.auth.application.dto.KakaoCallbackResult;

/**
 * 카카오 로그인 UseCase
 */
public interface KakaoLoginUseCase {

    /**
     * 카카오 인증 URL을 생성합니다.
     */
    String getKakaoAuthUrl();

    /**
     * 카카오 콜백을 처리합니다.
     */
    KakaoCallbackResult processCallback(String code);
}
