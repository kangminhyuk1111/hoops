package com.hoops.auth.adapter.in.web.dto;

import com.hoops.auth.application.port.in.SignupCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청
 */
public record SignupRequest(
        @NotBlank(message = "임시 토큰은 필수입니다")
        String tempToken,

        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다")
        String nickname
) {

    public SignupCommand toCommand() {
        return new SignupCommand(tempToken, nickname);
    }
}
