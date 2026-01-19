package com.hoops.auth.domain.vo;

/**
 * JWT 토큰 쌍 (Access Token + Refresh Token)
 */
public record TokenPair(
        String accessToken,
        String refreshToken
) {
    public static TokenPair of(String accessToken, String refreshToken) {
        return new TokenPair(accessToken, refreshToken);
    }
}
