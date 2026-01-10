package com.hoops.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hoops.auth.application.dto.AuthResult;
import com.hoops.auth.application.dto.TokenResult;
import com.hoops.user.application.exception.DuplicateNicknameException;
import com.hoops.user.application.exception.InvalidNicknameException;
import com.hoops.auth.application.port.in.SignupCommand;
import com.hoops.auth.application.port.out.JwtTokenProvider;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("유효한 닉네임(2~20자)으로 회원가입에 성공한다")
    void signup_WithValidNickname_Success() {
        // given
        Map<String, Object> claims = new HashMap<>();
        claims.put("kakaoId", "12345");
        claims.put("email", "test@example.com");
        claims.put("profileImage", "http://example.com/image.jpg");
        String tempToken = jwtTokenProvider.createTempToken(claims);

        SignupCommand command = new SignupCommand(tempToken, "유효한닉네임");

        // when
        AuthResult result = authService.signup(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.user().nickname()).isEqualTo("유효한닉네임");
    }

    @Test
    @DisplayName("닉네임이 2자 미만이면 회원가입에 실패한다")
    void signup_WithTooShortNickname_ThrowsException() {
        // given
        Map<String, Object> claims = new HashMap<>();
        claims.put("kakaoId", "12345");
        claims.put("email", "test@example.com");
        claims.put("profileImage", "http://example.com/image.jpg");
        String tempToken = jwtTokenProvider.createTempToken(claims);

        SignupCommand command = new SignupCommand(tempToken, "짧");

        // when & then
        assertThatThrownBy(() -> authService.signup(command))
                .isInstanceOf(InvalidNicknameException.class);
    }

    @Test
    @DisplayName("닉네임이 20자 초과이면 회원가입에 실패한다")
    void signup_WithTooLongNickname_ThrowsException() {
        // given
        Map<String, Object> claims = new HashMap<>();
        claims.put("kakaoId", "12345");
        claims.put("email", "test@example.com");
        claims.put("profileImage", "http://example.com/image.jpg");
        String tempToken = jwtTokenProvider.createTempToken(claims);

        String longNickname = "이닉네임은스무글자를초과하는매우긴닉네임입니다";
        SignupCommand command = new SignupCommand(tempToken, longNickname);

        // when & then
        assertThatThrownBy(() -> authService.signup(command))
                .isInstanceOf(InvalidNicknameException.class);
    }

    @Test
    @DisplayName("중복된 닉네임으로 회원가입하면 실패한다")
    void signup_WithDuplicateNickname_ThrowsException() {
        // given - 먼저 첫 번째 사용자 가입
        Map<String, Object> claims1 = new HashMap<>();
        claims1.put("kakaoId", "11111");
        claims1.put("email", "first@example.com");
        claims1.put("profileImage", "http://example.com/image1.jpg");
        String tempToken1 = jwtTokenProvider.createTempToken(claims1);
        authService.signup(new SignupCommand(tempToken1, "중복닉네임"));

        // given - 두 번째 사용자 가입 시도 (동일 닉네임)
        Map<String, Object> claims2 = new HashMap<>();
        claims2.put("kakaoId", "22222");
        claims2.put("email", "second@example.com");
        claims2.put("profileImage", "http://example.com/image2.jpg");
        String tempToken2 = jwtTokenProvider.createTempToken(claims2);
        SignupCommand command = new SignupCommand(tempToken2, "중복닉네임");

        // when & then
        assertThatThrownBy(() -> authService.signup(command))
                .isInstanceOf(DuplicateNicknameException.class);
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 새로운 토큰을 발급받는다")
    void refresh_WithValidRefreshToken_ReturnsNewTokens() {
        // given - 먼저 회원가입하여 토큰 획득
        Map<String, Object> claims = new HashMap<>();
        claims.put("kakaoId", "12345");
        claims.put("email", "test@example.com");
        claims.put("profileImage", "http://example.com/image.jpg");
        String tempToken = jwtTokenProvider.createTempToken(claims);
        AuthResult authResult = authService.signup(new SignupCommand(tempToken, "테스트유저"));

        // when
        TokenResult newTokens = authService.refresh(authResult.refreshToken());

        // then
        assertThat(newTokens).isNotNull();
        assertThat(newTokens.accessToken()).isNotBlank();
        assertThat(newTokens.refreshToken()).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(newTokens.accessToken())).isTrue();
    }
}
