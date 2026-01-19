package com.hoops.auth.application.dto;

/**
 * 회원가입 Command
 */
public record SignupCommand(
        String tempToken,
        String nickname
) {
}
