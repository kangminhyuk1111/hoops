package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.acceptance.mock.MockKakaoOAuthClient;
import com.hoops.auth.application.dto.KakaoUserInfo;
import com.hoops.auth.application.port.out.JwtTokenProvider;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 회원가입 기능 Step Definitions
 */
public class AuthSignupStepDefs {

    private final TestAdapter testAdapter;
    private final JwtTokenProvider jwtTokenProvider;
    private final MockKakaoOAuthClient mockKakaoOAuthClient;
    private final SharedTestContext sharedContext;

    public AuthSignupStepDefs(
            TestAdapter testAdapter,
            JwtTokenProvider jwtTokenProvider,
            MockKakaoOAuthClient mockKakaoOAuthClient,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mockKakaoOAuthClient = mockKakaoOAuthClient;
        this.sharedContext = sharedContext;
    }

    @먼저("카카오 인증이 완료된 신규 사용자이다")
    public void 카카오_인증이_완료된_신규_사용자이다() {
        String kakaoId = "new-user-kakao-" + UUID.randomUUID().toString().substring(0, 8);
        String email = "newuser" + System.currentTimeMillis() + "@kakao.com";

        Map<String, Object> claims = new HashMap<>();
        claims.put("kakaoId", kakaoId);
        claims.put("email", email);
        claims.put("profileImage", "https://example.com/profile.jpg");

        String tempToken = jwtTokenProvider.createTempToken(claims);

        sharedContext.setTempToken(tempToken);
        sharedContext.setKakaoId(kakaoId);
    }

    @만일("닉네임 {string}으로 회원가입을 요청한다")
    public void 닉네임으로_회원가입을_요청한다(String nickname) {
        Map<String, Object> request = new HashMap<>();
        request.put("tempToken", sharedContext.getTempToken());
        request.put("nickname", nickname);

        TestResponse response = testAdapter.post("/api/auth/signup", request);
        sharedContext.setLastResponse(response);

        if (response.statusCode() == 201) {
            Object accessToken = response.getJsonValue("accessToken");
            Object refreshToken = response.getJsonValue("refreshToken");
            if (accessToken != null) {
                sharedContext.setAccessToken(accessToken.toString());
            }
            if (refreshToken != null) {
                sharedContext.setRefreshToken(refreshToken.toString());
            }
        }
    }

    @만일("닉네임 없이 회원가입을 요청한다")
    public void 닉네임_없이_회원가입을_요청한다() {
        Map<String, Object> request = new HashMap<>();
        request.put("tempToken", sharedContext.getTempToken());

        TestResponse response = testAdapter.post("/api/auth/signup", request);
        sharedContext.setLastResponse(response);
    }

    @만일("유효하지 않은 임시 토큰으로 회원가입을 요청한다")
    public void 유효하지_않은_임시_토큰으로_회원가입을_요청한다() {
        Map<String, Object> request = new HashMap<>();
        request.put("tempToken", "invalid-temp-token");
        request.put("nickname", "테스트유저");

        TestResponse response = testAdapter.post("/api/auth/signup", request);
        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 액세스 토큰이 포함되어 있다")
    public void 응답에_액세스_토큰이_포함되어_있다() {
        TestResponse lastResponse = sharedContext.getLastResponse();
        assertThat(lastResponse.hasJsonField("accessToken"))
                .as("응답에 accessToken 필드가 포함되어 있어야 합니다")
                .isTrue();
        assertThat(lastResponse.getJsonValue("accessToken"))
                .as("accessToken 값이 비어있지 않아야 합니다")
                .isNotNull();
    }

    @그리고("응답에 리프레시 토큰이 포함되어 있다")
    public void 응답에_리프레시_토큰이_포함되어_있다() {
        TestResponse lastResponse = sharedContext.getLastResponse();
        assertThat(lastResponse.hasJsonField("refreshToken"))
                .as("응답에 refreshToken 필드가 포함되어 있어야 합니다")
                .isTrue();
        assertThat(lastResponse.getJsonValue("refreshToken"))
                .as("refreshToken 값이 비어있지 않아야 합니다")
                .isNotNull();
    }
}
