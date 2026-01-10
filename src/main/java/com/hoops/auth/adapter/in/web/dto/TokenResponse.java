package com.hoops.auth.adapter.in.web.dto;

import com.hoops.auth.application.dto.TokenResult;

/**
 * 토큰 응답
 */
public record TokenResponse(
        String accessToken,
        String refreshToken
) {

    public static TokenResponse from(TokenResult result) {
        return new TokenResponse(result.accessToken(), result.refreshToken());
    }
}
