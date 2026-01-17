package com.hoops.auth.application.service;

import com.hoops.auth.application.dto.AuthResult;
import com.hoops.auth.application.dto.TokenResult;
import com.hoops.auth.application.dto.UserInfo;
import com.hoops.auth.application.port.in.SignupCommand;
import com.hoops.auth.application.port.out.JwtTokenProvider;
import com.hoops.auth.application.port.out.UserInfoPort;
import com.hoops.auth.application.validator.SignupValidator;
import com.hoops.auth.domain.AuthAccount;
import com.hoops.auth.domain.repository.AuthAccountRepository;
import com.hoops.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SignupProcessor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserInfoPort userInfoPort;
    private final AuthAccountRepository authAccountRepository;
    private final SignupValidator signupValidator;

    public AuthResult process(SignupCommand command) {
        Map<String, Object> claims = signupValidator.validateAndExtractClaims(command);

        User savedUser = createUser(claims, command.nickname());
        TokenResult tokens = jwtTokenProvider.createTokens(savedUser.getId());
        createAuthAccount(claims, savedUser.getId(), tokens.refreshToken());

        return new AuthResult(tokens.accessToken(), tokens.refreshToken(), UserInfo.from(savedUser));
    }

    private User createUser(Map<String, Object> claims, String nickname) {
        String email = (String) claims.get("email");
        String profileImage = (String) claims.get("profileImage");

        User newUser = User.createNew(email, nickname, profileImage);
        return userInfoPort.save(newUser);
    }

    private void createAuthAccount(Map<String, Object> claims, Long userId, String refreshToken) {
        String kakaoId = (String) claims.get("kakaoId");
        AuthAccount authAccount = AuthAccount.createForKakao(userId, kakaoId, refreshToken);
        authAccountRepository.save(authAccount);
    }
}
