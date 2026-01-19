package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.auth.application.port.out.JwtTokenPort;
import com.hoops.user.domain.User;
import com.hoops.user.domain.repository.UserRepository;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class UserProfileStepDefs {

    private final TestAdapter testAdapter;
    private final UserRepository userRepository;
    private final JwtTokenPort jwtTokenProvider;
    private final SharedTestContext sharedContext;

    private User otherUser;

    public UserProfileStepDefs(
            TestAdapter testAdapter,
            UserRepository userRepository,
            JwtTokenPort jwtTokenProvider,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.sharedContext = sharedContext;
    }

    @먼저("사용자가 회원가입되어 있다")
    public void 사용자가_회원가입되어_있다() {
        User testUser = User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .rating(BigDecimal.valueOf(3.0))
                .totalMatches(0)
                .build();
        testUser = userRepository.save(testUser);
        sharedContext.setTestUser(testUser);
    }

    @그리고("사용자가 로그인되어 있다")
    public void 사용자가_로그인되어_있다() {
        User testUser = sharedContext.getTestUser();
        String accessToken = jwtTokenProvider.createTokens(testUser.getId()).accessToken();
        sharedContext.setAccessToken(accessToken);
    }

    @먼저("로그아웃 상태이다")
    public void 로그아웃_상태이다() {
        sharedContext.setAccessToken(null);
    }

    @만일("내 프로필 조회 API를 호출한다")
    public void 내_프로필_조회_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();
        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth("/api/users/me", accessToken);
        } else {
            response = testAdapter.get("/api/users/me");
        }
        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 사용자 정보가 포함되어 있다")
    public void 응답에_사용자_정보가_포함되어_있다() {
        TestResponse lastResponse = sharedContext.getLastResponse();
        User testUser = sharedContext.getTestUser();
        assertThat(lastResponse.hasJsonField("id")).isTrue();
        assertThat(lastResponse.hasJsonField("nickname")).isTrue();
        assertThat(lastResponse.getJsonValue("nickname")).isEqualTo(testUser.getNickname());
    }

    @먼저("다른 사용자가 존재한다")
    public void 다른_사용자가_존재한다() {
        User user = User.builder()
                .email("other" + System.currentTimeMillis() + "@example.com")
                .nickname("다른사용자")
                .rating(BigDecimal.valueOf(3.5))
                .totalMatches(5)
                .build();
        otherUser = userRepository.save(user);
    }

    @먼저("{string}을 사용하는 다른 사용자가 있다")
    public void 닉네임을_사용하는_다른_사용자가_있다(String nickname) {
        User user = User.builder()
                .email("other" + System.currentTimeMillis() + "@example.com")
                .nickname(nickname)
                .rating(BigDecimal.valueOf(3.0))
                .totalMatches(0)
                .build();
        userRepository.save(user);
    }

    @만일("해당 사용자 프로필 조회 API를 호출한다")
    public void 해당_사용자_프로필_조회_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();
        String path = "/api/users/" + otherUser.getId();

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth(path, accessToken);
        } else {
            response = testAdapter.get(path);
        }
        sharedContext.setLastResponse(response);
    }

    @만일("존재하지 않는 사용자 프로필 조회 API를 호출한다")
    public void 존재하지_않는_사용자_프로필_조회_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();
        Long nonExistentUserId = 999999L;
        String path = "/api/users/" + nonExistentUserId;

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth(path, accessToken);
        } else {
            response = testAdapter.get(path);
        }
        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 사용자 ID가 포함되어 있다")
    public void 응답에_사용자_ID가_포함되어_있다() {
        TestResponse response = sharedContext.getLastResponse();
        assertThat(response.hasJsonField("id"))
                .as("응답에 사용자 ID가 포함되어야 합니다")
                .isTrue();
    }

    @그리고("응답에 닉네임이 포함되어 있다")
    public void 응답에_닉네임이_포함되어_있다() {
        TestResponse response = sharedContext.getLastResponse();
        assertThat(response.hasJsonField("nickname"))
                .as("응답에 닉네임이 포함되어야 합니다")
                .isTrue();
    }

    @만일("닉네임을 {string}으로 수정 요청한다")
    public void 닉네임을_수정_요청한다(String newNickname) {
        String accessToken = sharedContext.getAccessToken();
        User testUser = sharedContext.getTestUser();
        String path = "/api/users/" + testUser.getId();

        Map<String, Object> requestBody = Map.of("nickname", newNickname);

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.putWithAuth(path, requestBody, accessToken);
        } else {
            response = testAdapter.putWithAuth(path, requestBody, null);
        }
        sharedContext.setLastResponse(response);
    }

    @만일("빈 닉네임으로 수정 요청한다")
    public void 빈_닉네임으로_수정_요청한다() {
        String accessToken = sharedContext.getAccessToken();
        User testUser = sharedContext.getTestUser();
        String path = "/api/users/" + testUser.getId();

        Map<String, Object> requestBody = Map.of("nickname", "");

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.putWithAuth(path, requestBody, accessToken);
        } else {
            response = testAdapter.putWithAuth(path, requestBody, null);
        }
        sharedContext.setLastResponse(response);
    }

    @그리고("응답의 닉네임이 {string} 이다")
    public void 응답의_닉네임이_이다(String expectedNickname) {
        TestResponse response = sharedContext.getLastResponse();
        assertThat(response.getJsonValue("nickname"))
                .as("응답의 닉네임이 %s 이어야 합니다", expectedNickname)
                .isEqualTo(expectedNickname);
    }
}
