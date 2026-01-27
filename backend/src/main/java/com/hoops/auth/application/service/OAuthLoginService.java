package com.hoops.auth.application.service;

import com.hoops.auth.application.dto.OAuthCallbackResult;
import com.hoops.auth.application.dto.UserInfo;
import com.hoops.auth.application.port.in.OAuthLoginUseCase;
import com.hoops.auth.application.port.out.OAuthPort;
import com.hoops.auth.application.port.out.JwtTokenPort;
import com.hoops.auth.application.port.out.UserInfoPort;
import com.hoops.auth.application.exception.UserNotFoundForAuthException;
import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.application.port.out.AuthAccountRepositoryPort;
import com.hoops.auth.domain.vo.AuthProvider;
import com.hoops.auth.domain.vo.AuthUserInfo;
import com.hoops.auth.domain.vo.OAuthTokenInfo;
import com.hoops.auth.domain.vo.OAuthUserInfo;
import com.hoops.auth.domain.vo.TempTokenClaims;
import com.hoops.auth.domain.vo.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OAuthLoginService implements OAuthLoginUseCase {

    private final OAuthPort oauthPort;
    private final JwtTokenPort jwtTokenPort;
    private final AuthAccountRepositoryPort authAccountRepository;
    private final UserInfoPort userInfoPort;

    @Override
    public String getAuthorizationUrl(AuthProvider provider) {
        return oauthPort.getAuthorizationUrl();
    }

    @Override
    public OAuthCallbackResult processCallback(AuthProvider provider, String code) {
        OAuthUserInfo oauthUserInfo = fetchOAuthUserInfo(code);

        Optional<AuthAccount> existingAccount = authAccountRepository
                .findByProviderAndProviderId(provider, oauthUserInfo.providerId());

        return existingAccount
                .map(this::handleExistingUser)
                .orElseGet(() -> handleNewUser(provider, oauthUserInfo));
    }

    private OAuthUserInfo fetchOAuthUserInfo(String code) {
        OAuthTokenInfo tokenInfo = oauthPort.getToken(code);
        return oauthPort.getUserInfo(tokenInfo.accessToken());
    }

    private OAuthCallbackResult handleExistingUser(AuthAccount authAccount) {
        AuthUserInfo userInfo = findUserByAuthAccount(authAccount);
        TokenPair tokens = jwtTokenPort.createTokens(userInfo.id());

        authAccountRepository.save(authAccount.withRefreshToken(tokens.refreshToken()));

        return OAuthCallbackResult.forExistingUser(
                tokens.accessToken(),
                tokens.refreshToken(),
                UserInfo.from(userInfo)
        );
    }

    private OAuthCallbackResult handleNewUser(AuthProvider provider, OAuthUserInfo oauthUserInfo) {
        TempTokenClaims claims = TempTokenClaims.fromOAuthUserInfo(provider, oauthUserInfo);
        String tempToken = jwtTokenPort.createTempToken(claims);
        return OAuthCallbackResult.forNewUser(tempToken, oauthUserInfo);
    }

    private AuthUserInfo findUserByAuthAccount(AuthAccount authAccount) {
        return userInfoPort.getUserInfo(authAccount.getUserId())
                .orElseThrow(() -> new UserNotFoundForAuthException(
                        authAccount.getId(), authAccount.getUserId()));
    }
}
