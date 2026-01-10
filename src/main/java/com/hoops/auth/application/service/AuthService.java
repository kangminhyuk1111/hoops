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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스
 *
 * 카카오 로그인, 회원가입, 토큰 갱신을 처리합니다.
 */
@Service
@Transactional
public class AuthService implements KakaoLoginUseCase, SignupUseCase, RefreshTokenUseCase {

    private static final int MIN_NICKNAME_LENGTH = 2;
    private static final int MAX_NICKNAME_LENGTH = 20;

    private final KakaoOAuthClient kakaoOAuthClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthAccountRepository authAccountRepository;
    private final UserRepository userRepository;

    public AuthService(
            KakaoOAuthClient kakaoOAuthClient,
            JwtTokenProvider jwtTokenProvider,
            AuthAccountRepository authAccountRepository,
            UserRepository userRepository) {
        this.kakaoOAuthClient = kakaoOAuthClient;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authAccountRepository = authAccountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String getKakaoAuthUrl() {
        return kakaoOAuthClient.getAuthorizationUrl();
    }

    @Override
    public KakaoCallbackResult processCallback(String code) {
        // 1. 인가코드로 카카오 토큰 교환
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.getToken(code);

        // 2. 카카오 사용자 정보 조회
        KakaoUserInfo kakaoUserInfo = kakaoOAuthClient.getUserInfo(tokenResponse.accessToken());

        // 3. 기존 회원 여부 확인
        Optional<AuthAccount> existingAccount = authAccountRepository
                .findByProviderAndProviderId(AuthProvider.KAKAO, kakaoUserInfo.kakaoId());

        if (existingAccount.isPresent()) {
            // 기존 회원: JWT 토큰 발급
            AuthAccount authAccount = existingAccount.get();
            User user = userRepository.findById(authAccount.getUserId())
                    .orElseThrow(() -> new IllegalStateException("AuthAccount exists but User not found"));

            TokenResult tokens = jwtTokenProvider.createTokens(user.getId());

            // RefreshToken 업데이트
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
            // 신규 회원: 임시 토큰 발급
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
        // 1. 임시 토큰 검증 및 클레임 추출
        Map<String, Object> claims = extractAndValidateTempToken(command.tempToken());

        String kakaoId = (String) claims.get("kakaoId");
        String email = (String) claims.get("email");
        String profileImage = (String) claims.get("profileImage");

        // 2. 닉네임 유효성 검증
        validateNickname(command.nickname());

        // 3. 닉네임 중복 확인
        if (userRepository.existsByNickname(command.nickname())) {
            throw new DuplicateNicknameException(command.nickname());
        }

        // 4. User 생성
        User newUser = new User(
                null,
                email,
                command.nickname(),
                profileImage,
                BigDecimal.ZERO,
                0
        );
        User savedUser = userRepository.save(newUser);

        // 5. JWT 토큰 생성
        TokenResult tokens = jwtTokenProvider.createTokens(savedUser.getId());

        // 6. AuthAccount 생성
        AuthAccount authAccount = new AuthAccount(
                null,
                savedUser.getId(),
                AuthProvider.KAKAO,
                kakaoId,
                null,
                tokens.refreshToken()
        );
        authAccountRepository.save(authAccount);

        // 7. 결과 반환
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
}
