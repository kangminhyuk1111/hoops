package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.match.domain.repository.MatchRepository;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;
import com.hoops.user.domain.model.User;
import com.hoops.user.domain.repository.UserRepository;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchUpdateStepDefs {

    private final TestAdapter testAdapter;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final SharedTestContext sharedContext;

    private LocalDate updatedMatchDate;

    public MatchUpdateStepDefs(
            TestAdapter testAdapter,
            MatchRepository matchRepository,
            UserRepository userRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.sharedContext = sharedContext;
    }

    @먼저("내가 생성한 경기에 {int}명이 참가하고 있다")
    public void 내가_생성한_경기에_명이_참가하고_있다(int participantCount) {
        User testUser = sharedContext.getTestUser();
        sharedContext.clearTestMatches();

        Match match = Match.builder()
                .version(0L)
                .hostId(testUser.getId())
                .hostNickname(testUser.getNickname())
                .title("내가 생성한 경기")
                .description("테스트 경기 설명")
                .latitude(BigDecimal.valueOf(37.5665))
                .longitude(BigDecimal.valueOf(126.9780))
                .address("서울시 중구")
                .matchDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .maxParticipants(10)
                .currentParticipants(participantCount)
                .status(MatchStatus.PENDING)
                .build();
        Match savedMatch = matchRepository.save(match);
        sharedContext.addTestMatch(savedMatch);
    }

    @만일("경기 제목을 {string}로 수정 요청한다")
    public void 경기_제목을_로_수정_요청한다(String newTitle) {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", newTitle);

        TestResponse response = testAdapter.putWithAuth(
                "/api/matches/" + match.getId(),
                requestBody,
                accessToken
        );
        sharedContext.setLastResponse(response);
    }

    @만일("경기 날짜와 시간을 수정 요청한다")
    public void 경기_날짜와_시간을_수정_요청한다() {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        updatedMatchDate = LocalDate.now().plusDays(14);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("matchDate", updatedMatchDate.toString());
        requestBody.put("startTime", "19:00");
        requestBody.put("endTime", "21:00");

        TestResponse response = testAdapter.putWithAuth(
                "/api/matches/" + match.getId(),
                requestBody,
                accessToken
        );
        sharedContext.setLastResponse(response);
    }

    @만일("최대 참가 인원을 {int}명으로 수정 요청한다")
    public void 최대_참가_인원을_명으로_수정_요청한다(int maxParticipants) {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("maxParticipants", maxParticipants);

        TestResponse response = testAdapter.putWithAuth(
                "/api/matches/" + match.getId(),
                requestBody,
                accessToken
        );
        sharedContext.setLastResponse(response);
    }

    @만일("존재하지 않는 경기 수정 API를 호출한다")
    public void 존재하지_않는_경기_수정_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();
        Long nonExistentMatchId = 999999L;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", "변경된 제목");

        TestResponse response = testAdapter.putWithAuth(
                "/api/matches/" + nonExistentMatchId,
                requestBody,
                accessToken
        );
        sharedContext.setLastResponse(response);
    }

    @그리고("응답의 제목이 {string} 이다")
    public void 응답의_제목이_이다(String expectedTitle) {
        TestResponse response = sharedContext.getLastResponse();
        String actualTitle = (String) response.getJsonValue("title");

        assertThat(actualTitle)
                .as("응답의 제목이 %s 이어야 합니다", expectedTitle)
                .isEqualTo(expectedTitle);
    }

    @그리고("응답의 경기 날짜가 변경되어 있다")
    public void 응답의_경기_날짜가_변경되어_있다() {
        TestResponse response = sharedContext.getLastResponse();
        String actualMatchDate = (String) response.getJsonValue("matchDate");

        assertThat(actualMatchDate)
                .as("응답의 경기 날짜가 변경되어 있어야 합니다")
                .isEqualTo(updatedMatchDate.toString());
    }

    @그리고("응답의 최대 참가 인원이 {int} 이다")
    public void 응답의_최대_참가_인원이_이다(int expectedMaxParticipants) {
        TestResponse response = sharedContext.getLastResponse();
        int actualMaxParticipants = (Integer) response.getJsonValue("maxParticipants");

        assertThat(actualMaxParticipants)
                .as("응답의 최대 참가 인원이 %d 이어야 합니다", expectedMaxParticipants)
                .isEqualTo(expectedMaxParticipants);
    }
}
