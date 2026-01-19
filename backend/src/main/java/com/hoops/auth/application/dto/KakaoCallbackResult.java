package com.hoops.auth.application.dto;

import com.hoops.auth.domain.vo.KakaoUserInfo;

/**
 * 카카오 콜백 처리 결과
 */
public record KakaoCallbackResult(
        boolean isNewUser,
        String tempToken,
        String accessToken,
        String refreshToken,
        KakaoUserInfo kakaoInfo,
        UserInfo user
) {

    public static KakaoCallbackResult forNewUser(String tempToken, KakaoUserInfo kakaoInfo) {
        return new KakaoCallbackResult(true, tempToken, null, null, kakaoInfo, null);
    }

    public static KakaoCallbackResult forExistingUser(String accessToken, String refreshToken, UserInfo user) {
        return new KakaoCallbackResult(false, null, accessToken, refreshToken, null, user);
    }
}
