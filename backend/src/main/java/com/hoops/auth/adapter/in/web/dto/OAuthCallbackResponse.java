package com.hoops.auth.adapter.in.web.dto;

import com.hoops.auth.application.dto.OAuthCallbackResult;

/**
 * OAuth 콜백 응답 DTO
 */
public record OAuthCallbackResponse(
        boolean isNewUser,
        String tempToken,
        String accessToken,
        String refreshToken,
        OAuthUserResponse oauthInfo,
        UserResponse user
) {

    public static OAuthCallbackResponse from(OAuthCallbackResult result) {
        return new OAuthCallbackResponse(
                result.isNewUser(),
                result.tempToken(),
                result.accessToken(),
                result.refreshToken(),
                result.oauthInfo() != null ? OAuthUserResponse.from(result.oauthInfo()) : null,
                result.user() != null ? UserResponse.from(result.user()) : null
        );
    }
}
