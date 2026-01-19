package com.hoops.auth.application.port.in;

import com.hoops.auth.application.dto.OAuthCallbackResult;
import com.hoops.auth.domain.vo.AuthProvider;

/**
 * OAuth 로그인 UseCase (벤더 중립적)
 */
public interface OAuthLoginUseCase {

    /**
     * OAuth 인증 URL을 생성합니다.
     */
    String getAuthorizationUrl(AuthProvider provider);

    /**
     * OAuth 콜백을 처리합니다.
     */
    OAuthCallbackResult processCallback(AuthProvider provider, String code);
}
