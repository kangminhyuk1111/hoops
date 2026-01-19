package com.hoops.auth.domain.vo;

/**
 * OAuth 사용자 정보 (벤더 중립적)
 */
public record OAuthUserInfo(
        String providerId,
        String email,
        String nickname,
        String profileImage
) {
    public static OAuthUserInfo of(String providerId, String email, String nickname, String profileImage) {
        return new OAuthUserInfo(providerId, email, nickname, profileImage);
    }
}
