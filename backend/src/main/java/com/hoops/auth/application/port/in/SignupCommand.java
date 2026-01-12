package com.hoops.auth.application.port.in;

/**
 * 회원가입 완료 커맨드
 *
 * 회원가입에 필요한 데이터를 전달합니다.
 */
public record SignupCommand(
        String tempToken,
        String nickname
) {
}
