package com.hoops.auth.adapter.in.web.dto;

import com.hoops.auth.application.dto.AuthResult;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(
                result.accessToken(),
                result.refreshToken(),
                UserResponse.from(result.user())
        );
    }
}
