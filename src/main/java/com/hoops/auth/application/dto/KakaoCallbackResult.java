package com.hoops.auth.application.dto;

/**
 * 카카오 콜백 처리 결과
 *
 * 신규 회원인 경우: isNewUser=true, tempToken 포함, accessToken/refreshToken은 null
 * 기존 회원인 경우: isNewUser=false, accessToken/refreshToken 포함, tempToken은 null
 */
public record KakaoCallbackResult(
        boolean isNewUser,
        String tempToken,
        String accessToken,
        String refreshToken,
        KakaoUserInfo kakaoInfo,
        UserInfo user
) {

    /**
     * 신규 회원용 결과 생성
     */
    public static KakaoCallbackResult forNewUser(String tempToken, KakaoUserInfo kakaoInfo) {
        return new KakaoCallbackResult(true, tempToken, null, null, kakaoInfo, null);
    }

    /**
     * 기존 회원용 결과 생성
     */
    public static KakaoCallbackResult forExistingUser(String accessToken, String refreshToken, UserInfo user) {
        return new KakaoCallbackResult(false, null, accessToken, refreshToken, null, user);
    }
}
