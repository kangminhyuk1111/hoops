package com.hoops.auth.application.service;

import com.hoops.auth.application.dto.KakaoCallbackResult;
import com.hoops.auth.application.dto.KakaoTokenResponse;
import com.hoops.auth.application.dto.KakaoUserInfo;
import com.hoops.auth.application.dto.TokenResult;
import com.hoops.auth.application.dto.UserInfo;
import com.hoops.auth.application.exception.UserNotFoundForAuthException;
import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.model.AuthProvider;
import com.hoops.auth.domain.port.AuthAccountPort;
import com.hoops.auth.domain.port.JwtTokenProvider;
import com.hoops.auth.domain.port.KakaoOAuthClient;
import com.hoops.auth.domain.port.UserInfoPort;
import com.hoops.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles Kakao OAuth callback processing.
 */
@Component
@RequiredArgsConstructor
public class KakaoCallbackHandler {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthAccountPort authAccountPort;
    private final UserInfoPort userInfoPort;

    public KakaoCallbackResult handle(String code) {
        KakaoUserInfo kakaoUserInfo = fetchKakaoUserInfo(code);

        Optional<AuthAccount> existingAccount = authAccountPort
                .findByProviderAndProviderId(AuthProvider.KAKAO, kakaoUserInfo.kakaoId());

        return existingAccount
                .map(this::handleExistingUser)
                .orElseGet(() -> handleNewUser(kakaoUserInfo));
    }

    private KakaoUserInfo fetchKakaoUserInfo(String code) {
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.getToken(code);
        return kakaoOAuthClient.getUserInfo(tokenResponse.accessToken());
    }

    private KakaoCallbackResult handleExistingUser(AuthAccount authAccount) {
        User user = findUserByAuthAccount(authAccount);
        TokenResult tokens = jwtTokenProvider.createTokens(user.getId());

        authAccountPort.save(authAccount.withRefreshToken(tokens.refreshToken()));

        return KakaoCallbackResult.forExistingUser(tokens.accessToken(), tokens.refreshToken(), UserInfo.from(user));
    }

    private KakaoCallbackResult handleNewUser(KakaoUserInfo kakaoUserInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("kakaoId", kakaoUserInfo.kakaoId());
        claims.put("email", kakaoUserInfo.email());
        claims.put("profileImage", kakaoUserInfo.profileImage());

        String tempToken = jwtTokenProvider.createTempToken(claims);
        return KakaoCallbackResult.forNewUser(tempToken, kakaoUserInfo);
    }

    private User findUserByAuthAccount(AuthAccount authAccount) {
        return userInfoPort.findById(authAccount.getUserId())
                .orElseThrow(() -> new UserNotFoundForAuthException(authAccount.getId(), authAccount.getUserId()));
    }
}
