package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.match.domain.model.Match;
import com.hoops.participation.domain.model.Participation;
import com.hoops.participation.domain.vo.ParticipationStatus;
import com.hoops.participation.domain.repository.ParticipationRepository;
import com.hoops.user.domain.model.User;
import com.hoops.user.domain.repository.UserRepository;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchParticipantsStepDefs {

    private final TestAdapter testAdapter;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final SharedTestContext sharedContext;

    public MatchParticipantsStepDefs(
            TestAdapter testAdapter,
            UserRepository userRepository,
            ParticipationRepository participationRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.userRepository = userRepository;
        this.participationRepository = participationRepository;
        this.sharedContext = sharedContext;
    }

    @그리고("다른 사용자 {int}명이 해당 경기에 참가했다")
    public void 다른_사용자_N명이_해당_경기에_참가했다(int count) {
        Match match = sharedContext.getTestMatches().get(0);

        for (int i = 0; i < count; i++) {
            User participant = User.builder()
                    .email("participant" + (i + 1) + "@example.com")
                    .nickname("참가자" + (i + 1))
                    .rating(BigDecimal.valueOf(3.0))
                    .totalMatches(0)
                    .build();
            User savedParticipant = userRepository.save(participant);

            Participation participation = Participation.reconstitute(
                    null, null, match.getId(), savedParticipant.getId(),
                    ParticipationStatus.CONFIRMED, LocalDateTime.now());
            participationRepository.save(participation);
        }
    }

    @만일("해당 경기의 참가자 목록 조회 API를 호출한다")
    public void 해당_경기의_참가자_목록_조회_API를_호출한다() {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth("/api/matches/" + match.getId() + "/participants", accessToken);
        } else {
            response = testAdapter.get("/api/matches/" + match.getId() + "/participants");
        }
        sharedContext.setLastResponse(response);
    }

    @만일("존재하지 않는 경기의 참가자 목록 조회 API를 호출한다")
    public void 존재하지_않는_경기의_참가자_목록_조회_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();
        Long nonExistentMatchId = 999999L;

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth("/api/matches/" + nonExistentMatchId + "/participants", accessToken);
        } else {
            response = testAdapter.get("/api/matches/" + nonExistentMatchId + "/participants");
        }
        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 참가자가 {int}명 포함되어 있다")
    public void 응답에_참가자가_N명_포함되어_있다(int expectedCount) {
        TestResponse lastResponse = sharedContext.getLastResponse();
        int actualCount = lastResponse.getJsonArraySize();
        assertThat(actualCount)
                .as("참가자 수가 %d명 이어야 합니다", expectedCount)
                .isEqualTo(expectedCount);
    }

    @그리고("{string}라는 닉네임의 사용자가 해당 경기에 참가했다")
    public void 닉네임의_사용자가_해당_경기에_참가했다(String nickname) {
        Match match = sharedContext.getTestMatches().get(0);

        User participant = User.builder()
                .email("participant" + System.currentTimeMillis() + "@example.com")
                .nickname(nickname)
                .rating(BigDecimal.valueOf(3.5))
                .totalMatches(2)
                .build();
        User savedParticipant = userRepository.save(participant);

        Participation participation = Participation.reconstitute(
                null, null, match.getId(), savedParticipant.getId(),
                ParticipationStatus.CONFIRMED, LocalDateTime.now());
        participationRepository.save(participation);
    }

    @그리고("첫번째 참가자의 닉네임이 {string} 이다")
    public void 첫번째_참가자의_닉네임이_이다(String expectedNickname) {
        TestResponse response = sharedContext.getLastResponse();
        var participants = response.getJsonList("$");
        assertThat(participants).isNotEmpty();

        Object nickname = participants.get(0).get("nickname");
        assertThat(nickname)
                .as("첫번째 참가자의 닉네임이 %s 이어야 합니다", expectedNickname)
                .isEqualTo(expectedNickname);
    }
}
