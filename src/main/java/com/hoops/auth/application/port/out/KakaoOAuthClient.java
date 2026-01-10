package com.hoops.auth.application.port.out;

import com.hoops.auth.application.dto.KakaoTokenResponse;
import com.hoops.auth.application.dto.KakaoUserInfo;

/**
 * 카카오 OAuth 클라이언트 인터페이스
 *
 * 카카오 OAuth API와 통신하는 Outbound Port입니다.
 */
public interface KakaoOAuthClient {

    /**
     * 카카오 인증 URL을 생성합니다.
     *
     * @return 카카오 인증 페이지 URL
     */
    String getAuthorizationUrl();

    /**
     * 인가코드로 액세스 토큰을 교환합니다.
     *
     * @param code 카카오 인가코드
     * @return 카카오 토큰 응답
     */
    KakaoTokenResponse getToken(String code);

    /**
     * 액세스 토큰으로 사용자 정보를 조회합니다.
     *
     * @param accessToken 카카오 액세스 토큰
     * @return 카카오 사용자 정보
     */
    KakaoUserInfo getUserInfo(String accessToken);
}
