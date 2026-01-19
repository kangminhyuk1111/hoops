package com.hoops.auth.application.service;

import com.hoops.auth.application.dto.AuthResult;
import com.hoops.auth.application.dto.KakaoCallbackResult;
import com.hoops.auth.application.dto.SignupCommand;
import com.hoops.auth.application.dto.TokenResult;
import com.hoops.auth.application.port.in.KakaoLoginUseCase;
import com.hoops.auth.application.port.in.RefreshTokenUseCase;
import com.hoops.auth.application.port.in.SignupUseCase;
import com.hoops.auth.domain.port.JwtTokenProvider;
import com.hoops.auth.domain.port.KakaoOAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService implements KakaoLoginUseCase, SignupUseCase, RefreshTokenUseCase {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoCallbackHandler kakaoCallbackHandler;
    private final SignupProcessor signupProcessor;

    @Override
    public String getKakaoAuthUrl() {
        return kakaoOAuthClient.getAuthorizationUrl();
    }

    @Override
    public KakaoCallbackResult processCallback(String code) {
        return kakaoCallbackHandler.handle(code);
    }

    @Override
    public AuthResult signup(SignupCommand command) {
        return signupProcessor.process(command);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResult refresh(String refreshToken) {
        return jwtTokenProvider.refreshTokens(refreshToken);
    }
}
