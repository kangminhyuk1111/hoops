package com.hoops.auth.application.service;

import com.hoops.auth.application.port.in.TokenUseCase;
import com.hoops.auth.application.port.out.JwtTokenPort;
import com.hoops.auth.domain.vo.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TokenService implements TokenUseCase {

    private final JwtTokenPort jwtTokenPort;

    @Override
    public TokenPair refresh(String refreshToken) {
        return jwtTokenPort.refreshTokens(refreshToken);
    }
}
