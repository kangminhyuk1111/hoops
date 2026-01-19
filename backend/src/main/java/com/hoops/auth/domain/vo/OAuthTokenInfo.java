package com.hoops.auth.domain.vo;

/**
 * OAuth 토큰 정보 (벤더 중립적)
 */
public record OAuthTokenInfo(
        String accessToken,
        String refreshToken,
        Integer expiresIn
) {
    public static OAuthTokenInfo of(String accessToken, String refreshToken, Integer expiresIn) {
        return new OAuthTokenInfo(accessToken, refreshToken, expiresIn);
    }
}
