package com.hoops.auth.application.dto;

/**
 * 카카오 사용자 정보
 */
public record KakaoUserInfo(
        String kakaoId,
        String email,
        String nickname,
        String profileImage
) {
}
