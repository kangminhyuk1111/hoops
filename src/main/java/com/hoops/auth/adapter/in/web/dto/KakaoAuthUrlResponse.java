package com.hoops.auth.adapter.in.web.dto;

/**
 * 카카오 인증 URL 응답
 */
public record KakaoAuthUrlResponse(
        String authUrl
) {
}
