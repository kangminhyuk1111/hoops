package com.hoops.auth.application.service;

import com.hoops.auth.application.dto.KakaoCallbackResult;
import com.hoops.auth.application.dto.UserInfo;
import com.hoops.auth.application.port.in.KakaoLoginUseCase;
import com.hoops.auth.application.port.out.AuthAccountPort;
import com.hoops.auth.application.port.out.JwtTokenPort;
import com.hoops.auth.application.port.out.KakaoOAuthPort;
import com.hoops.auth.application.port.out.UserInfoPort;
import com.hoops.auth.application.exception.UserNotFoundForAuthException;
import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.vo.AuthProvider;
import com.hoops.auth.domain.vo.AuthUserInfo;
import com.hoops.auth.domain.vo.KakaoTokenInfo;
import com.hoops.auth.domain.vo.KakaoUserInfo;
import com.hoops.auth.domain.vo.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class KakaoLoginService implements KakaoLoginUseCase {

    private final KakaoOAuthPort kakaoOAuthPort;
    private final JwtTokenPort jwtTokenPort;
    private final AuthAccountPort authAccountPort;
    private final UserInfoPort userInfoPort;

    @Override
    public String getKakaoAuthUrl() {
        return kakaoOAuthPort.getAuthorizationUrl();
    }

    @Override
    public KakaoCallbackResult processCallback(String code) {
        KakaoUserInfo kakaoUserInfo = fetchKakaoUserInfo(code);

        Optional<AuthAccount> existingAccount = authAccountPort
                .findByProviderAndProviderId(AuthProvider.KAKAO, kakaoUserInfo.kakaoId());

        return existingAccount
                .map(this::handleExistingUser)
                .orElseGet(() -> handleNewUser(kakaoUserInfo));
    }

    private KakaoUserInfo fetchKakaoUserInfo(String code) {
        KakaoTokenInfo tokenInfo = kakaoOAuthPort.getToken(code);
        return kakaoOAuthPort.getUserInfo(tokenInfo.accessToken());
    }

    private KakaoCallbackResult handleExistingUser(AuthAccount authAccount) {
        AuthUserInfo userInfo = findUserByAuthAccount(authAccount);
        TokenPair tokens = jwtTokenPort.createTokens(userInfo.id());

        authAccountPort.save(authAccount.withRefreshToken(tokens.refreshToken()));

        return KakaoCallbackResult.forExistingUser(
                tokens.accessToken(),
                tokens.refreshToken(),
                UserInfo.from(userInfo)
        );
    }

    private KakaoCallbackResult handleNewUser(KakaoUserInfo kakaoUserInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("kakaoId", kakaoUserInfo.kakaoId());
        claims.put("email", kakaoUserInfo.email());
        claims.put("profileImage", kakaoUserInfo.profileImage());

        String tempToken = jwtTokenPort.createTempToken(claims);
        return KakaoCallbackResult.forNewUser(tempToken, kakaoUserInfo);
    }

    private AuthUserInfo findUserByAuthAccount(AuthAccount authAccount) {
        return userInfoPort.findById(authAccount.getUserId())
                .orElseThrow(() -> new UserNotFoundForAuthException(
                        authAccount.getId(), authAccount.getUserId()));
    }
}
