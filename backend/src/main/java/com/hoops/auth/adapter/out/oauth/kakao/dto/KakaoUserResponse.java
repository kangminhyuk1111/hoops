package com.hoops.auth.adapter.out.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoops.auth.domain.vo.OAuthUserInfo;

/**
 * Kakao user info API response DTO.
 */
public record KakaoUserResponse(
        Long id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    public OAuthUserInfo toOAuthUserInfo() {
        String email = kakaoAccount != null ? kakaoAccount.email() : null;
        String nickname = extractNickname();
        String profileImage = extractProfileImage();

        return OAuthUserInfo.of(String.valueOf(id), email, nickname, profileImage);
    }

    private String extractNickname() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().nickname();
    }

    private String extractProfileImage() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().profileImageUrl();
    }

    public record KakaoAccount(
            String email,
            KakaoProfile profile
    ) {}

    public record KakaoProfile(
            String nickname,
            @JsonProperty("profile_image_url") String profileImageUrl
    ) {}
}
