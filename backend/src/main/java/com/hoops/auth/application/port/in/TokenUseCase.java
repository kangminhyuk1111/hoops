package com.hoops.auth.application.port.in;

import com.hoops.auth.domain.vo.TokenPair;

/**
 * 토큰 관련 UseCase
 */
public interface TokenUseCase {

    /**
     * 토큰을 갱신합니다.
     */
    TokenPair refresh(String refreshToken);
}
