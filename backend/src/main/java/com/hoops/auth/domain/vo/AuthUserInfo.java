package com.hoops.auth.domain.vo;

/**
 * Auth 도메인 내에서 사용하는 사용자 정보
 */
public record AuthUserInfo(
        Long id,
        String nickname,
        String email,
        String profileImage
) {
    public static AuthUserInfo of(Long id, String nickname, String email, String profileImage) {
        return new AuthUserInfo(id, nickname, email, profileImage);
    }
}
