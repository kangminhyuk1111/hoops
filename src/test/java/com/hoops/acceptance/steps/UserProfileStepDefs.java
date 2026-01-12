package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.auth.application.port.out.JwtTokenProvider;
import com.hoops.user.domain.User;
import com.hoops.user.domain.repository.UserRepository;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class UserProfileStepDefs {

    private final TestAdapter testAdapter;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SharedTestContext sharedContext;

    public UserProfileStepDefs(
            TestAdapter testAdapter,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
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
}
