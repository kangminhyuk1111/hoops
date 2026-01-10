package com.hoops.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 갱신 요청
 */
public record RefreshTokenRequest(
        @NotBlank(message = "리프레시 토큰은 필수입니다")
        String refreshToken
) {
}
