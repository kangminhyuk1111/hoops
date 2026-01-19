package com.hoops.auth.adapter.in.web.dto;

import com.hoops.auth.domain.vo.OAuthUserInfo;

/**
 * OAuth 사용자 정보 응답 DTO
 */
public record OAuthUserResponse(
        String email,
        String nickname,
        String profileImage
) {

    public static OAuthUserResponse from(OAuthUserInfo info) {
        return new OAuthUserResponse(
                info.email(),
                info.nickname(),
                info.profileImage()
        );
    }
}
