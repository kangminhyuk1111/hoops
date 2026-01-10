package com.hoops.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hoops.auth.application.dto.TokenResult;
import com.hoops.user.application.exception.InvalidRefreshTokenException;
import com.hoops.user.application.exception.InvalidTempTokenException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderImplTest {

    private JwtTokenProviderImpl jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "test-secret-key-must-be-at-least-32-characters-long",
                1800000L,   // 30분
                1209600000L, // 14일
                600000L     // 10분
        );
        jwtTokenProvider = new JwtTokenProviderImpl(properties);
    }

    @Test
    @DisplayName("userId로 Access Token과 Refresh Token을 생성한다")
    void createTokens_WithUserId_ReturnsAccessAndRefreshTokens() {
        // given
        Long userId = 1L;

        // when
        TokenResult result = jwtTokenProvider.createTokens(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.accessToken()).isNotEqualTo(result.refreshToken());
    }

    @Test
    @DisplayName("토큰에서 userId를 추출한다")
    void getUserIdFromToken_WithValidToken_ReturnsUserId() {
        // given
        Long userId = 123L;
        TokenResult tokens = jwtTokenProvider.createTokens(userId);

        // when
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(tokens.accessToken());

        // then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("유효한 토큰은 검증에 성공한다")
    void validateToken_WithValidToken_ReturnsTrue() {
        // given
        TokenResult tokens = jwtTokenProvider.createTokens(1L);

        // when
        boolean isValid = jwtTokenProvider.validateToken(tokens.accessToken());

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("변조된 토큰은 검증에 실패한다")
    void validateToken_WithTamperedToken_ReturnsFalse() {
        // given
        String tamperedToken = "invalid.token.here";

        // when
        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("클레임을 포함한 임시 토큰을 생성한다")
    void createTempToken_WithClaims_ReturnsTempToken() {
        // given
        Map<String, Object> claims = new HashMap<>();
        claims.put("kakaoId", "12345");
        claims.put("email", "test@example.com");

        // when
        String tempToken = jwtTokenProvider.createTempToken(claims);

        // then
        assertThat(tempToken).isNotBlank();
    }

    @Test
    @DisplayName("임시 토큰에서 클레임을 추출한다")
    void getClaimsFromTempToken_WithValidToken_ReturnsClaims() {
        // given
        Map<String, Object> claims = new HashMap<>();
        claims.put("kakaoId", "12345");
        claims.put("email", "test@example.com");
        String tempToken = jwtTokenProvider.createTempToken(claims);

        // when
        Map<String, Object> extractedClaims = jwtTokenProvider.getClaimsFromTempToken(tempToken);

        // then
        assertThat(extractedClaims.get("kakaoId")).isEqualTo("12345");
        assertThat(extractedClaims.get("email")).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("잘못된 임시 토큰은 예외를 발생시킨다")
    void getClaimsFromTempToken_WithInvalidToken_ThrowsException() {
        // given
        String invalidToken = "invalid.temp.token";

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.getClaimsFromTempToken(invalidToken))
                .isInstanceOf(InvalidTempTokenException.class);
    }

    @Test
    @DisplayName("Refresh 토큰으로 새로운 토큰을 발급한다")
    void refreshTokens_WithValidRefreshToken_ReturnsNewTokens() {
        // given
        Long userId = 1L;
        TokenResult originalTokens = jwtTokenProvider.createTokens(userId);

        // when
        TokenResult newTokens = jwtTokenProvider.refreshTokens(originalTokens.refreshToken());

        // then
        assertThat(newTokens.accessToken()).isNotBlank();
        assertThat(newTokens.refreshToken()).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(newTokens.accessToken())).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(newTokens.accessToken())).isEqualTo(userId);
    }

    @Test
    @DisplayName("Access 토큰으로 갱신 시도 시 예외를 발생시킨다")
    void refreshTokens_WithAccessToken_ThrowsException() {
        // given
        TokenResult tokens = jwtTokenProvider.createTokens(1L);

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.refreshTokens(tokens.accessToken()))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("만료된 토큰은 검증에 실패한다")
    void validateToken_WithExpiredToken_ReturnsFalse() throws InterruptedException {
        // given - 1ms 만료 시간으로 설정
        JwtProperties shortExpiryProps = new JwtProperties(
                "test-secret-key-must-be-at-least-32-characters-long",
                1L,
                1L,
                1L
        );
        JwtTokenProviderImpl shortExpiryProvider = new JwtTokenProviderImpl(shortExpiryProps);
        TokenResult tokens = shortExpiryProvider.createTokens(1L);

        // when - 토큰 만료 대기
        Thread.sleep(10);

        // then
        assertThat(shortExpiryProvider.validateToken(tokens.accessToken())).isFalse();
    }
}
