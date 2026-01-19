package com.hoops.auth.adapter.in.web.dto;

import com.hoops.auth.domain.vo.TokenPair;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
    public static TokenResponse from(TokenPair tokenPair) {
        return new TokenResponse(tokenPair.accessToken(), tokenPair.refreshToken());
    }
}
