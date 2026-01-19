package com.hoops.auth.adapter.in.web.dto;

import com.hoops.auth.application.dto.UserInfo;

public record UserResponse(
        Long id,
        String nickname,
        String email,
        String profileImage
) {
    public static UserResponse from(UserInfo userInfo) {
        return new UserResponse(
                userInfo.id(),
                userInfo.nickname(),
                userInfo.email(),
                userInfo.profileImage()
        );
    }
}
