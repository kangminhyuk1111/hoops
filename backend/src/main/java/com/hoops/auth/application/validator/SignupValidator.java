package com.hoops.auth.application.validator;

import com.hoops.auth.application.dto.SignupCommand;
import com.hoops.auth.domain.port.JwtTokenProvider;
import com.hoops.auth.domain.port.UserInfoPort;
import com.hoops.user.application.exception.DuplicateNicknameException;
import com.hoops.user.application.exception.InvalidNicknameException;
import com.hoops.user.application.exception.InvalidTempTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Validates signup requests.
 */
@Component
@RequiredArgsConstructor
public class SignupValidator {

    private static final int MIN_NICKNAME_LENGTH = 2;
    private static final int MAX_NICKNAME_LENGTH = 20;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserInfoPort userInfoPort;

    public Map<String, Object> validateAndExtractClaims(SignupCommand command) {
        Map<String, Object> claims = extractTempTokenClaims(command.tempToken());
        validateNickname(command.nickname());
        validateNicknameNotDuplicated(command.nickname());
        return claims;
    }

    private Map<String, Object> extractTempTokenClaims(String tempToken) {
        try {
            return jwtTokenProvider.getClaimsFromTempToken(tempToken);
        } catch (Exception e) {
            throw new InvalidTempTokenException();
        }
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new InvalidNicknameException(nickname, "Nickname is required");
        }

        String trimmedNickname = nickname.trim();
        boolean isTooShort = trimmedNickname.length() < MIN_NICKNAME_LENGTH;
        boolean isTooLong = trimmedNickname.length() > MAX_NICKNAME_LENGTH;

        if (isTooShort || isTooLong) {
            throw new InvalidNicknameException(nickname);
        }
    }

    private void validateNicknameNotDuplicated(String nickname) {
        if (userInfoPort.existsByNickname(nickname)) {
            throw new DuplicateNicknameException(nickname);
        }
    }
}
