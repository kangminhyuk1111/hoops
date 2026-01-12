package com.hoops.user.adapter.in.web.dto;

import com.hoops.user.application.port.in.UpdateUserProfileCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 사용자 프로필 수정 요청 DTO
 */
public record UpdateUserRequest(
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다")
        String nickname,

        String profileImage
) {
    public UpdateUserProfileCommand toCommand(Long userId, Long requesterId) {
        return new UpdateUserProfileCommand(userId, requesterId, nickname, profileImage);
    }
}
