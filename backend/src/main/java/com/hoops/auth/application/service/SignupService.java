package com.hoops.auth.application.service;

import com.hoops.auth.application.dto.AuthResult;
import com.hoops.auth.application.dto.SignupCommand;
import com.hoops.auth.application.dto.UserInfo;
import com.hoops.auth.application.port.in.SignupUseCase;
import com.hoops.auth.application.port.out.JwtTokenPort;
import com.hoops.auth.application.port.out.UserInfoPort;
import com.hoops.auth.application.exception.DuplicateNicknameException;
import com.hoops.auth.application.exception.InvalidTempTokenException;
import com.hoops.auth.domain.exception.InvalidNicknameException;
import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.repository.AuthAccountRepository;
import com.hoops.auth.domain.vo.AuthProvider;
import com.hoops.auth.domain.vo.AuthUserInfo;
import com.hoops.auth.application.dto.CreateUserRequest;
import com.hoops.auth.domain.vo.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class SignupService implements SignupUseCase {

    private static final int MIN_NICKNAME_LENGTH = 2;
    private static final int MAX_NICKNAME_LENGTH = 20;

    private final JwtTokenPort jwtTokenPort;
    private final UserInfoPort userInfoPort;
    private final AuthAccountRepository authAccountRepository;

    @Override
    public AuthResult signup(SignupCommand command) {
        Map<String, Object> claims = validateAndExtractClaims(command);

        AuthUserInfo savedUserInfo = createUser(claims, command.nickname());
        TokenPair tokens = jwtTokenPort.createTokens(savedUserInfo.id());
        createAuthAccount(claims, savedUserInfo.id(), tokens.refreshToken());

        return new AuthResult(
                tokens.accessToken(),
                tokens.refreshToken(),
                UserInfo.from(savedUserInfo)
        );
    }

    private Map<String, Object> validateAndExtractClaims(SignupCommand command) {
        Map<String, Object> claims = extractTempTokenClaims(command.tempToken());
        validateNickname(command.nickname());
        validateNicknameNotDuplicated(command.nickname());
        return claims;
    }

    private Map<String, Object> extractTempTokenClaims(String tempToken) {
        try {
            return jwtTokenPort.getClaimsFromTempToken(tempToken);
        } catch (Exception e) {
            throw new InvalidTempTokenException();
        }
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new InvalidNicknameException(nickname, "Nickname is required");
        }

        String trimmedNickname = nickname.trim();
        if (trimmedNickname.length() < MIN_NICKNAME_LENGTH ||
            trimmedNickname.length() > MAX_NICKNAME_LENGTH) {
            throw new InvalidNicknameException(nickname);
        }
    }

    private void validateNicknameNotDuplicated(String nickname) {
        if (userInfoPort.existsByNickname(nickname)) {
            throw new DuplicateNicknameException(nickname);
        }
    }

    private AuthUserInfo createUser(Map<String, Object> claims, String nickname) {
        String email = (String) claims.get("email");
        String profileImage = (String) claims.get("profileImage");

        CreateUserRequest request = CreateUserRequest.of(email, nickname, profileImage);
        return userInfoPort.createUser(request);
    }

    private void createAuthAccount(Map<String, Object> claims, Long userId, String refreshToken) {
        AuthProvider provider = AuthProvider.valueOf((String) claims.get("provider"));
        String providerId = (String) claims.get("providerId");
        AuthAccount authAccount = AuthAccount.create(userId, provider, providerId, refreshToken);
        authAccountRepository.save(authAccount);
    }
}
