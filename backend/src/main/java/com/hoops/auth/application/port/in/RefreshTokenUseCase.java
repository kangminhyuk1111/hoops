package com.hoops.auth.application.port.in;

import com.hoops.auth.application.dto.TokenResult;

/**
 * 토큰 갱신 Use Case
 *
 * Refresh Token으로 새로운 Access Token을 발급합니다.
 */
public interface RefreshTokenUseCase {

    /**
     * 토큰을 갱신합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 토큰 정보
     */
    TokenResult refresh(String refreshToken);
}
