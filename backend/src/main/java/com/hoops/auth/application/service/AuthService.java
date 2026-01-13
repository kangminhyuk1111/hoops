package com.hoops.auth.application.service;

import com.hoops.auth.application.dto.AuthResult;
import com.hoops.auth.application.dto.KakaoCallbackResult;
import com.hoops.auth.application.dto.KakaoTokenResponse;
import com.hoops.auth.application.dto.KakaoUserInfo;
import com.hoops.auth.application.dto.TokenResult;
import com.hoops.auth.application.dto.UserInfo;
import com.hoops.auth.application.port.in.KakaoLoginUseCase;
import com.hoops.auth.application.port.in.RefreshTokenUseCase;
import com.hoops.auth.application.port.in.SignupCommand;
import com.hoops.auth.application.port.in.SignupUseCase;
import com.hoops.auth.application.port.in.TestLoginUseCase;
import com.hoops.auth.application.port.out.JwtTokenProvider;
import com.hoops.auth.application.port.out.KakaoOAuthClient;
import com.hoops.auth.domain.AuthAccount;
import com.hoops.auth.domain.AuthProvider;
import com.hoops.auth.domain.repository.AuthAccountRepository;
import com.hoops.user.application.exception.DuplicateNicknameException;
import com.hoops.user.application.exception.InvalidNicknameException;
import com.hoops.user.application.exception.InvalidTempTokenException;
import com.hoops.user.domain.User;
import com.hoops.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService implements KakaoLoginUseCase, SignupUseCase, RefreshTokenUseCase, TestLoginUseCase {

    private static final int MIN_NICKNAME_LENGTH = 2;
    private static final int MAX_NICKNAME_LENGTH = 20;

    private final KakaoOAuthClient kakaoOAuthClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthAccountRepository authAccountRepository;
    private final UserRepository userRepository;

    @Override
    public String getKakaoAuthUrl() {
        return kakaoOAuthClient.getAuthorizationUrl();
    }

    @Override
    public KakaoCallbackResult processCallback(String code) {
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.getToken(code);
        KakaoUserInfo kakaoUserInfo = kakaoOAuthClient.getUserInfo(tokenResponse.accessToken());

        Optional<AuthAccount> existingAccount = authAccountRepository
                .findByProviderAndProviderId(AuthProvider.KAKAO, kakaoUserInfo.kakaoId());

        if (existingAccount.isPresent()) {
            AuthAccount authAccount = existingAccount.get();
            User user = userRepository.findById(authAccount.getUserId())
                    .orElseThrow(() -> new IllegalStateException("AuthAccount exists but User not found"));

            TokenResult tokens = jwtTokenProvider.createTokens(user.getId());
            updateRefreshToken(authAccount, tokens.refreshToken());

            UserInfo userInfo = new UserInfo(
                    user.getId(),
                    user.getNickname(),
                    user.getEmail(),
                    user.getProfileImage()
            );

            return KakaoCallbackResult.forExistingUser(
                    tokens.accessToken(),
                    tokens.refreshToken(),
                    userInfo
            );
        } else {
            Map<String, Object> claims = new HashMap<>();
            claims.put("kakaoId", kakaoUserInfo.kakaoId());
            claims.put("email", kakaoUserInfo.email());
            claims.put("profileImage", kakaoUserInfo.profileImage());

            String tempToken = jwtTokenProvider.createTempToken(claims);

            return KakaoCallbackResult.forNewUser(tempToken, kakaoUserInfo);
        }
    }

    @Override
    public AuthResult signup(SignupCommand command) {
        Map<String, Object> claims = extractAndValidateTempToken(command.tempToken());

        String kakaoId = (String) claims.get("kakaoId");
        String email = (String) claims.get("email");
        String profileImage = (String) claims.get("profileImage");

        validateNickname(command.nickname());

        if (userRepository.existsByNickname(command.nickname())) {
            throw new DuplicateNicknameException(command.nickname());
        }

        User newUser = new User(
                null,
                email,
                command.nickname(),
                profileImage,
                BigDecimal.ZERO,
                0
        );
        User savedUser = userRepository.save(newUser);

        TokenResult tokens = jwtTokenProvider.createTokens(savedUser.getId());

        AuthAccount authAccount = new AuthAccount(
                null,
                savedUser.getId(),
                AuthProvider.KAKAO,
                kakaoId,
                null,
                tokens.refreshToken()
        );
        authAccountRepository.save(authAccount);

        UserInfo userInfo = new UserInfo(
                savedUser.getId(),
                savedUser.getNickname(),
                savedUser.getEmail(),
                savedUser.getProfileImage()
        );

        return new AuthResult(tokens.accessToken(), tokens.refreshToken(), userInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResult refresh(String refreshToken) {
        return jwtTokenProvider.refreshTokens(refreshToken);
    }

    private Map<String, Object> extractAndValidateTempToken(String tempToken) {
        try {
            return jwtTokenProvider.getClaimsFromTempToken(tempToken);
        } catch (Exception e) {
            throw new InvalidTempTokenException();
        }
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new InvalidNicknameException(nickname, "닉네임은 필수입니다");
        }

        String trimmedNickname = nickname.trim();
        if (trimmedNickname.length() < MIN_NICKNAME_LENGTH
                || trimmedNickname.length() > MAX_NICKNAME_LENGTH) {
            throw new InvalidNicknameException(nickname);
        }
    }

    private void updateRefreshToken(AuthAccount authAccount, String newRefreshToken) {
        AuthAccount updatedAccount = new AuthAccount(
                authAccount.getId(),
                authAccount.getUserId(),
                authAccount.getProvider(),
                authAccount.getProviderId(),
                authAccount.getPasswordHash(),
                newRefreshToken
        );
        authAccountRepository.save(updatedAccount);
    }

    @Override
    public AuthResult testLogin() {
        String testEmail = "test@hoops.kr";
        String testNickname = "TestUser";
        String testProviderId = "test_user_001";

        Optional<AuthAccount> existingAccount = authAccountRepository
                .findByProviderAndProviderId(AuthProvider.KAKAO, testProviderId);

        User user;
        AuthAccount authAccount;

        if (existingAccount.isPresent()) {
            authAccount = existingAccount.get();
            user = userRepository.findById(authAccount.getUserId())
                    .orElseThrow(() -> new IllegalStateException("AuthAccount exists but User not found"));
        } else {
            user = new User(
                    null,
                    testEmail,
                    testNickname,
                    null,
                    BigDecimal.valueOf(4.5),
                    10
            );
            User savedUser = userRepository.save(user);
            user = savedUser;

            authAccount = new AuthAccount(
                    null,
                    savedUser.getId(),
                    AuthProvider.KAKAO,
                    testProviderId,
                    null,
                    null
            );
            authAccount = authAccountRepository.save(authAccount);
        }

        TokenResult tokens = jwtTokenProvider.createTokens(user.getId());

        AuthAccount updatedAccount = new AuthAccount(
                authAccount.getId(),
                authAccount.getUserId(),
                authAccount.getProvider(),
                authAccount.getProviderId(),
                authAccount.getPasswordHash(),
                tokens.refreshToken()
        );
        authAccountRepository.save(updatedAccount);

        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getProfileImage()
        );

        return new AuthResult(tokens.accessToken(), tokens.refreshToken(), userInfo);
    }
}
