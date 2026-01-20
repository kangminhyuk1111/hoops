package com.hoops.auth.adapter.out.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoops.auth.domain.vo.OAuthTokenInfo;

/**
 * Kakao OAuth token API response DTO.
 */
public record KakaoTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") Integer expiresIn,
        @JsonProperty("token_type") String tokenType
) {
    public OAuthTokenInfo toOAuthTokenInfo() {
        return OAuthTokenInfo.of(accessToken, refreshToken, expiresIn);
    }
}
