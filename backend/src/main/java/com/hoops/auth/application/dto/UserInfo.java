package com.hoops.auth.application.dto;

/**
 * 사용자 정보 (응답용)
 */
public record UserInfo(
        Long id,
        String nickname,
        String email,
        String profileImage
) {
}
