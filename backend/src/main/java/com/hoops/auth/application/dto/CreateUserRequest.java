package com.hoops.auth.application.dto;

/**
 * 사용자 생성 요청
 */
public record CreateUserRequest(
        String email,
        String nickname,
        String profileImage
) {
    public static CreateUserRequest of(String email, String nickname, String profileImage) {
        return new CreateUserRequest(email, nickname, profileImage);
    }
}
