package com.hoops.auth.domain.vo;

/**
 * Kakao OAuth 토큰 정보
 */
public record KakaoTokenInfo(
        String accessToken,
        String refreshToken,
        Integer expiresIn
) {
}
