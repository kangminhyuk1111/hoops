package com.hoops.auth.application.dto;

/**
 * Signup completion command.
 *
 * Contains data required for signup process.
 */
public record SignupCommand(
        String tempToken,
        String nickname
) {
}
