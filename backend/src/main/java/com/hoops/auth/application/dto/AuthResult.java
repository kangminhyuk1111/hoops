package com.hoops.auth.application.dto;

/**
 * 인증 결과 (로그인/회원가입 성공 시)
 */
public record AuthResult(
        String accessToken,
        String refreshToken,
        UserInfo user
) {
}
