package com.hoops.auth.application.dto;

import com.hoops.auth.domain.vo.AuthUserInfo;

/**
 * 사용자 정보 (응답용)
 */
public record UserInfo(
        Long id,
        String nickname,
        String email,
        String profileImage
) {
    public static UserInfo from(AuthUserInfo authUserInfo) {
        return new UserInfo(
                authUserInfo.id(),
                authUserInfo.nickname(),
                authUserInfo.email(),
                authUserInfo.profileImage()
        );
    }
}
