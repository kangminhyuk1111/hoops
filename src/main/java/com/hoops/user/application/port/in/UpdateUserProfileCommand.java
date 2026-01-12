package com.hoops.user.application.port.in;

/**
 * 사용자 프로필 수정 커맨드
 */
public record UpdateUserProfileCommand(
        Long userId,
        Long requesterId,
        String nickname,
        String profileImage
) {
    public UpdateUserProfileCommand {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다");
        }
        if (requesterId == null) {
            throw new IllegalArgumentException("requesterId는 필수입니다");
        }
    }
}
