package com.hoops.auth.application.dto;

import com.hoops.auth.domain.vo.OAuthUserInfo;

/**
 * OAuth 콜백 처리 결과 (벤더 중립적)
 */
public record OAuthCallbackResult(
        boolean isNewUser,
        String tempToken,
        String accessToken,
        String refreshToken,
        OAuthUserInfo oauthInfo,
        UserInfo user
) {

    public static OAuthCallbackResult forNewUser(String tempToken, OAuthUserInfo oauthInfo) {
        return new OAuthCallbackResult(true, tempToken, null, null, oauthInfo, null);
    }

    public static OAuthCallbackResult forExistingUser(String accessToken, String refreshToken, UserInfo user) {
        return new OAuthCallbackResult(false, null, accessToken, refreshToken, null, user);
    }
}
