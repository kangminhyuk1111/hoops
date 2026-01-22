package com.hoops.auth.application.dto;

/**
 * 사용자 생성 Command
 */
public record CreateUserCommand(
        String email,
        String nickname,
        String profileImage
) {
    public static CreateUserCommand of(String email, String nickname, String profileImage) {
        return new CreateUserCommand(email, nickname, profileImage);
    }
}
