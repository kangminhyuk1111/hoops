package com.hoops.auth.application.port.out;

import com.hoops.auth.domain.vo.TokenPair;

import java.util.Map;

/**
 * JWT 토큰 Port
 */
public interface JwtTokenPort {

    /**
     * Access Token과 Refresh Token을 생성합니다.
     */
    TokenPair createTokens(Long userId);

    /**
     * 임시 토큰을 생성합니다 (회원가입 전 사용).
     */
    String createTempToken(Map<String, Object> claims);

    /**
     * 토큰에서 userId를 추출합니다.
     */
    Long getUserIdFromToken(String token);

    /**
     * 임시 토큰에서 클레임을 추출합니다.
     */
    Map<String, Object> getClaimsFromTempToken(String tempToken);

    /**
     * Refresh Token으로 새로운 토큰을 발급합니다.
     */
    TokenPair refreshTokens(String refreshToken);

    /**
     * 토큰 유효성을 검증합니다.
     */
    boolean validateToken(String token);
}
