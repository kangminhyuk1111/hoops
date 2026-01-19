package com.hoops.auth.adapter.in.web.dto;

import com.hoops.auth.application.dto.SignupCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "Temp token is required")
        String tempToken,

        @NotBlank(message = "Nickname is required")
        @Size(min = 2, max = 20, message = "Nickname must be between 2 and 20 characters")
        String nickname
) {
    public SignupCommand toCommand() {
        return new SignupCommand(tempToken, nickname);
    }
}
