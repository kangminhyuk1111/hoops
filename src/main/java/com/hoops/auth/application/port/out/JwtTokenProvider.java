package com.hoops.auth.application.port.out;

import com.hoops.auth.application.dto.TokenResult;
import java.util.Map;

/**
 * JWT 토큰 제공자 인터페이스
 *
 * JWT 토큰 생성 및 검증을 담당하는 Outbound Port입니다.
 */
public interface JwtTokenProvider {

    /**
     * Access Token과 Refresh Token을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return 토큰 결과
     */
    TokenResult createTokens(Long userId);

    /**
     * 임시 토큰을 생성합니다. (회원가입 완료 전 사용)
     *
     * @param claims 토큰에 포함할 클레임
     * @return 임시 토큰
     */
    String createTempToken(Map<String, Object> claims);

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 임시 토큰에서 클레임을 추출합니다.
     *
     * @param tempToken 임시 토큰
     * @return 클레임 맵
     */
    Map<String, Object> getClaimsFromTempToken(String tempToken);

    /**
     * Refresh Token으로 새로운 토큰을 발급합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 토큰 결과
     */
    TokenResult refreshTokens(String refreshToken);

    /**
     * 토큰의 유효성을 검증합니다.
     *
     * @param token JWT 토큰
     * @return 유효 여부
     */
    boolean validateToken(String token);
}
