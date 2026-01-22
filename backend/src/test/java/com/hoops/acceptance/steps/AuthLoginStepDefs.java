package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.acceptance.mock.MockKakaoOAuthClient;
import com.hoops.auth.application.port.out.JwtTokenPort;
import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.repository.AuthAccountRepository;
import com.hoops.auth.domain.vo.AuthProvider;
import com.hoops.auth.domain.vo.OAuthUserInfo;
import com.hoops.auth.domain.vo.TokenPair;
import com.hoops.user.domain.model.User;
import com.hoops.user.domain.repository.UserRepository;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 로그인 기능 Step Definitions
 */
public class AuthLoginStepDefs {

    private final TestAdapter testAdapter;
    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final JwtTokenPort jwtTokenProvider;
    private final MockKakaoOAuthClient mockKakaoOAuthClient;
    private final SharedTestContext sharedContext;

    public AuthLoginStepDefs(
            TestAdapter testAdapter,
            UserRepository userRepository,
            AuthAccountRepository authAccountRepository,
            JwtTokenPort jwtTokenProvider,
            MockKakaoOAuthClient mockKakaoOAuthClient,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.userRepository = userRepository;
        this.authAccountRepository = authAccountRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mockKakaoOAuthClient = mockKakaoOAuthClient;
        this.sharedContext = sharedContext;
    }

    @먼저("이미 가입된 회원이 있다")
    public void 이미_가입된_회원이_있다() {
        String kakaoId = "existing-kakao-" + UUID.randomUUID().toString().substring(0, 8);
        String email = "existing" + System.currentTimeMillis() + "@kakao.com";
        String code = "existing-code-" + UUID.randomUUID().toString().substring(0, 8);

        User user = User.reconstitute(
                null,
                email,
                "기존회원",
                "https://example.com/profile.jpg",
                BigDecimal.valueOf(3.0),
                0
        );
        User savedUser = userRepository.save(user);
        sharedContext.setTestUser(savedUser);

        AuthAccount authAccount = new AuthAccount(
                null,
                savedUser.getId(),
                AuthProvider.KAKAO,
                kakaoId,
                null,
                null
        );
        authAccountRepository.save(authAccount);

        OAuthUserInfo oauthUserInfo = OAuthUserInfo.of(
                kakaoId,
                email,
                "기존회원",
                "https://example.com/profile.jpg"
        );
        mockKakaoOAuthClient.registerUser(code, oauthUserInfo);

        sharedContext.setKakaoCode(code);
        sharedContext.setKakaoId(kakaoId);
    }

    @만일("해당 회원이 카카오 인증을 완료한다")
    public void 해당_회원이_카카오_인증을_완료한다() {
        String code = sharedContext.getKakaoCode();
        TestResponse response = testAdapter.get("/api/auth/kakao/callback?code=" + code);
        sharedContext.setLastResponse(response);

        if (response.statusCode() == 200) {
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

    @먼저("가입되지 않은 카카오 계정이다")
    public void 가입되지_않은_카카오_계정이다() {
        String kakaoId = "new-kakao-" + UUID.randomUUID().toString().substring(0, 8);
        String email = "newuser" + System.currentTimeMillis() + "@kakao.com";
        String code = "new-code-" + UUID.randomUUID().toString().substring(0, 8);

        OAuthUserInfo oauthUserInfo = OAuthUserInfo.of(
                kakaoId,
                email,
                "신규유저",
                "https://example.com/profile.jpg"
        );
        mockKakaoOAuthClient.registerUser(code, oauthUserInfo);

        sharedContext.setKakaoCode(code);
        sharedContext.setKakaoId(kakaoId);
    }

    @만일("해당 사용자가 카카오 인증을 완료한다")
    public void 해당_사용자가_카카오_인증을_완료한다() {
        String code = sharedContext.getKakaoCode();
        TestResponse response = testAdapter.get("/api/auth/kakao/callback?code=" + code);
        sharedContext.setLastResponse(response);

        if (response.statusCode() == 202) {
            Object tempToken = response.getJsonValue("tempToken");
            if (tempToken != null) {
                sharedContext.setTempToken(tempToken.toString());
            }
        }
    }

    @그리고("응답에 임시 토큰이 포함되어 있다")
    public void 응답에_임시_토큰이_포함되어_있다() {
        TestResponse lastResponse = sharedContext.getLastResponse();
        assertThat(lastResponse.hasJsonField("tempToken"))
                .as("응답에 tempToken 필드가 포함되어 있어야 합니다")
                .isTrue();
        assertThat(lastResponse.getJsonValue("tempToken"))
                .as("tempToken 값이 비어있지 않아야 합니다")
                .isNotNull();
    }

    @그리고("응답에 신규 사용자 여부가 true 이다")
    public void 응답에_신규_사용자_여부가_true_이다() {
        TestResponse lastResponse = sharedContext.getLastResponse();
        assertThat(lastResponse.hasJsonField("isNewUser"))
                .as("응답에 isNewUser 필드가 포함되어 있어야 합니다")
                .isTrue();
        assertThat(lastResponse.getJsonValue("isNewUser"))
                .as("isNewUser 값이 true 여야 합니다")
                .isEqualTo(true);
    }

    @먼저("로그인된 사용자가 있다")
    public void 로그인된_사용자가_있다() {
        String kakaoId = "loggedin-kakao-" + UUID.randomUUID().toString().substring(0, 8);
        String email = "loggedin" + System.currentTimeMillis() + "@kakao.com";

        User user = User.reconstitute(
                null,
                email,
                "로그인유저",
                "https://example.com/profile.jpg",
                BigDecimal.valueOf(3.0),
                0
        );
        User savedUser = userRepository.save(user);
        sharedContext.setTestUser(savedUser);

        TokenPair tokens = jwtTokenProvider.createTokens(savedUser.getId());
        sharedContext.setAccessToken(tokens.accessToken());
        sharedContext.setRefreshToken(tokens.refreshToken());

        AuthAccount authAccount = new AuthAccount(
                null,
                savedUser.getId(),
                AuthProvider.KAKAO,
                kakaoId,
                null,
                tokens.refreshToken()
        );
        authAccountRepository.save(authAccount);
    }

    @만일("리프레시 토큰으로 토큰 갱신을 요청한다")
    public void 리프레시_토큰으로_토큰_갱신을_요청한다() {
        Map<String, Object> request = new HashMap<>();
        request.put("refreshToken", sharedContext.getRefreshToken());

        TestResponse response = testAdapter.post("/api/auth/refresh", request);
        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 새로운 액세스 토큰이 포함되어 있다")
    public void 응답에_새로운_액세스_토큰이_포함되어_있다() {
        TestResponse lastResponse = sharedContext.getLastResponse();
        assertThat(lastResponse.hasJsonField("accessToken"))
                .as("응답에 accessToken 필드가 포함되어 있어야 합니다")
                .isTrue();
        Object newAccessToken = lastResponse.getJsonValue("accessToken");
        assertThat(newAccessToken)
                .as("accessToken 값이 비어있지 않아야 합니다")
                .isNotNull();
    }
}
