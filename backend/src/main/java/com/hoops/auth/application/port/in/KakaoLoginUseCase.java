package com.hoops.auth.application.port.in;

import com.hoops.auth.application.dto.KakaoCallbackResult;

/**
 * 카카오 로그인 Use Case
 *
 * 카카오 OAuth 인증 플로우를 처리합니다.
 */
public interface KakaoLoginUseCase {

    /**
     * 카카오 인증 URL을 생성합니다.
     *
     * @return 카카오 인증 페이지 URL
     */
    String getKakaoAuthUrl();

    /**
     * 카카오 콜백을 처리합니다.
     * 인가코드로 토큰을 교환하고 사용자 정보를 조회합니다.
     *
     * @param code 카카오 인가코드
     * @return 콜백 처리 결과 (신규/기존 회원 여부, 토큰 정보)
     */
    KakaoCallbackResult processCallback(String code);
}
