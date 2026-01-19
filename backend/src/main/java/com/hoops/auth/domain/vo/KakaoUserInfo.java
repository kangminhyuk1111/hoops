package com.hoops.auth.domain.vo;

/**
 * Kakao 사용자 정보
 */
public record KakaoUserInfo(
        String kakaoId,
        String email,
        String nickname,
        String profileImage
) {
}
