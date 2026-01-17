package com.hoops.user.application.port.in;

import com.hoops.common.exception.InvalidCommandException;

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
            throw new InvalidCommandException("userId", "필수 값입니다");
        }
        if (requesterId == null) {
            throw new InvalidCommandException("requesterId", "필수 값입니다");
        }
    }
}
