package com.hoops.auth.domain.port;

import com.hoops.auth.application.dto.KakaoTokenResponse;
import com.hoops.auth.application.dto.KakaoUserInfo;

/**
 * Kakao OAuth Client Interface.
 *
 * Outbound port for communication with Kakao OAuth API.
 */
public interface KakaoOAuthClient {

    /**
     * Generates Kakao authorization URL.
     *
     * @return Kakao authorization page URL
     */
    String getAuthorizationUrl();

    /**
     * Exchanges authorization code for access token.
     *
     * @param code Kakao authorization code
     * @return Kakao token response
     */
    KakaoTokenResponse getToken(String code);

    /**
     * Retrieves user information using access token.
     *
     * @param accessToken Kakao access token
     * @return Kakao user information
     */
    KakaoUserInfo getUserInfo(String accessToken);
}
