package com.hoops.auth.application.dto;

import com.hoops.user.domain.User;

/**
 * 사용자 정보 (응답용)
 */
public record UserInfo(
        Long id,
        String nickname,
        String email,
        String profileImage
) {
    public static UserInfo from(User user) {
        return new UserInfo(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getProfileImage()
        );
    }
}
