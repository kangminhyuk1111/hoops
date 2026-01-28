package com.hoops.auth.application.port.out;

import com.hoops.auth.domain.vo.AuthProvider;
import com.hoops.auth.domain.vo.OAuthTokenInfo;
import com.hoops.auth.domain.vo.OAuthUserInfo;

/**
 * OAuth 인증 Port (벤더 중립적)
 */
public interface OAuthPort {

    /**
     * 이 Adapter가 지원하는 Provider를 반환합니다.
     */
    AuthProvider getProvider();

    /**
     * OAuth 인증 URL을 반환합니다.
     */
    String getAuthorizationUrl();

    /**
     * 인가 코드로 토큰을 교환합니다.
     */
    OAuthTokenInfo getToken(String code);

    /**
     * 액세스 토큰으로 사용자 정보를 조회합니다.
     */
    OAuthUserInfo getUserInfo(String accessToken);
}
