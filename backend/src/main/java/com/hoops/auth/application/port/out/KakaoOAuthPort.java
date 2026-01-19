package com.hoops.auth.application.port.out;

import com.hoops.auth.domain.vo.KakaoTokenInfo;
import com.hoops.auth.domain.vo.KakaoUserInfo;

/**
 * Kakao OAuth Port
 */
public interface KakaoOAuthPort {

    /**
     * 카카오 인증 URL을 생성합니다.
     */
    String getAuthorizationUrl();

    /**
     * 인가코드로 토큰을 교환합니다.
     */
    KakaoTokenInfo getToken(String code);

    /**
     * 액세스 토큰으로 사용자 정보를 조회합니다.
     */
    KakaoUserInfo getUserInfo(String accessToken);
}
