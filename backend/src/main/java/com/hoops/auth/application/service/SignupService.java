package com.hoops.auth.application.service;

import com.hoops.auth.application.dto.AuthResult;
import com.hoops.auth.application.dto.CreateUserCommand;
import com.hoops.auth.application.dto.SignupCommand;
import com.hoops.auth.application.dto.UserInfo;
import com.hoops.auth.application.exception.DuplicateNicknameException;
import com.hoops.auth.application.exception.InvalidTempTokenException;
import com.hoops.auth.application.port.in.SignupUseCase;
import com.hoops.auth.application.port.out.JwtTokenPort;
import com.hoops.auth.application.port.out.UserInfoPort;
import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.repository.AuthAccountRepository;
import com.hoops.auth.domain.vo.AuthUserInfo;
import com.hoops.auth.domain.vo.Nickname;
import com.hoops.auth.domain.vo.TempTokenClaims;
import com.hoops.auth.domain.vo.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SignupService implements SignupUseCase {

    private final JwtTokenPort jwtTokenPort;
    private final UserInfoPort userInfoPort;
    private final AuthAccountRepository authAccountRepository;

    @Override
    public AuthResult signup(SignupCommand command) {
        TempTokenClaims claims = extractTempTokenClaims(command.tempToken());
        Nickname nickname = Nickname.of(command.nickname());
        validateNicknameNotDuplicated(nickname);

        AuthUserInfo savedUserInfo = createUser(claims, nickname);
        TokenPair tokens = jwtTokenPort.createTokens(savedUserInfo.id());
        createAuthAccount(claims, savedUserInfo.id(), tokens.refreshToken());

        return new AuthResult(
                tokens.accessToken(),
                tokens.refreshToken(),
                UserInfo.from(savedUserInfo)
        );
    }

    private TempTokenClaims extractTempTokenClaims(String tempToken) {
        try {
            return jwtTokenPort.getClaimsFromTempToken(tempToken);
        } catch (Exception e) {
            throw new InvalidTempTokenException();
        }
    }

    private void validateNicknameNotDuplicated(Nickname nickname) {
        if (userInfoPort.existsByNickname(nickname.value())) {
            throw new DuplicateNicknameException(nickname.value());
        }
    }

    private AuthUserInfo createUser(TempTokenClaims claims, Nickname nickname) {
        CreateUserCommand createUserCommand = CreateUserCommand.of(
                claims.email(),
                nickname.value(),
                claims.profileImage()
        );
        return userInfoPort.createUser(createUserCommand);
    }

    private void createAuthAccount(TempTokenClaims claims, Long userId, String refreshToken) {
        AuthAccount authAccount = AuthAccount.create(
                userId,
                claims.provider(),
                claims.providerId(),
                refreshToken
        );
        authAccountRepository.save(authAccount);
    }
}
