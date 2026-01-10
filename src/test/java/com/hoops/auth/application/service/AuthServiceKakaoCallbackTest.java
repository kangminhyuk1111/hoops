package com.hoops.auth.application.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.hoops.auth.application.dto.KakaoCallbackResult;
import com.hoops.auth.application.port.in.SignupCommand;
import com.hoops.auth.application.port.out.JwtTokenProvider;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class AuthServiceKakaoCallbackTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("kakao.auth-base-url", () -> "http://localhost:" + wireMockServer.port());
        registry.add("kakao.api-base-url", () -> "http://localhost:" + wireMockServer.port());
    }

    @Test
    @DisplayName("신규 회원의 카카오 콜백 처리 시 임시 토큰을 발급한다")
    void processCallback_NewUser_ReturnsTempToken() {
        // given
        String authCode = "test-auth-code";
        String kakaoAccessToken = "kakao-access-token";

        stubKakaoTokenApi(authCode, kakaoAccessToken);
        stubKakaoUserInfoApi(kakaoAccessToken, "12345", "newuser@example.com", "신규유저", "http://example.com/profile.jpg");

        // when
        KakaoCallbackResult result = authService.processCallback(authCode);

        // then
        assertThat(result.isNewUser()).isTrue();
        assertThat(result.tempToken()).isNotBlank();
        assertThat(result.accessToken()).isNull();
        assertThat(result.refreshToken()).isNull();
        assertThat(result.kakaoInfo()).isNotNull();
        assertThat(result.kakaoInfo().kakaoId()).isEqualTo("12345");
        assertThat(result.kakaoInfo().email()).isEqualTo("newuser@example.com");
    }

    @Test
    @DisplayName("기존 회원의 카카오 콜백 처리 시 액세스 토큰을 발급한다")
    void processCallback_ExistingUser_ReturnsAccessToken() {
        // given - 먼저 회원가입으로 기존 회원 생성
        String kakaoId = "99999";
        String email = "existing@example.com";

        Map<String, Object> claims = new HashMap<>();
        claims.put("kakaoId", kakaoId);
        claims.put("email", email);
        claims.put("profileImage", "http://example.com/existing.jpg");
        String tempToken = jwtTokenProvider.createTempToken(claims);
        authService.signup(new SignupCommand(tempToken, "기존회원"));

        // given - 카카오 API 모킹
        String authCode = "existing-user-auth-code";
        String kakaoAccessToken = "kakao-access-token-existing";

        stubKakaoTokenApi(authCode, kakaoAccessToken);
        stubKakaoUserInfoApi(kakaoAccessToken, kakaoId, email, "기존유저닉네임", "http://example.com/existing.jpg");

        // when
        KakaoCallbackResult result = authService.processCallback(authCode);

        // then
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.tempToken()).isNull();
        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.user()).isNotNull();
        assertThat(result.user().nickname()).isEqualTo("기존회원");
    }

    private void stubKakaoTokenApi(String authCode, String accessToken) {
        wireMockServer.stubFor(post(urlPathEqualTo("/oauth/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "access_token": "%s",
                                    "refresh_token": "kakao-refresh-token",
                                    "expires_in": 3600
                                }
                                """.formatted(accessToken))));
    }

    private void stubKakaoUserInfoApi(String accessToken, String kakaoId, String email, String nickname, String profileImage) {
        wireMockServer.stubFor(get(urlPathEqualTo("/v2/user/me"))
                .withHeader("Authorization", equalTo("Bearer " + accessToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": %s,
                                    "kakao_account": {
                                        "email": "%s",
                                        "profile": {
                                            "nickname": "%s",
                                            "profile_image_url": "%s"
                                        }
                                    }
                                }
                                """.formatted(kakaoId, email, nickname, profileImage))));
    }
}
