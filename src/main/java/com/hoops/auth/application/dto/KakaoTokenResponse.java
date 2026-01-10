package com.hoops.auth.application.dto;

/**
 * 카카오 토큰 응답
 */
public record KakaoTokenResponse(
        String accessToken,
        String refreshToken,
        Integer expiresIn
) {
}
