package com.hoops.auth.application.dto;

/**
 * JWT 토큰 결과
 */
public record TokenResult(
        String accessToken,
        String refreshToken
) {
}
