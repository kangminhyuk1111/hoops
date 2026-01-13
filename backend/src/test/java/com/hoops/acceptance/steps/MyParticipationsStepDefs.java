package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import com.hoops.match.domain.MatchStatus;
import com.hoops.participation.domain.Participation;
import com.hoops.participation.domain.ParticipationStatus;
import com.hoops.participation.domain.repository.ParticipationRepository;
import com.hoops.user.domain.User;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

public class MyParticipationsStepDefs {

    private final TestAdapter testAdapter;
    private final MatchRepository matchRepository;
    private final ParticipationRepository participationRepository;
    private final SharedTestContext sharedContext;

    public MyParticipationsStepDefs(
            TestAdapter testAdapter,
            MatchRepository matchRepository,
            ParticipationRepository participationRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.matchRepository = matchRepository;
        this.participationRepository = participationRepository;
        this.sharedContext = sharedContext;
    }

    @먼저("경기가 {int}개 등록되어 있다")
    public void 경기가_N개_등록되어_있다(int count) {
        User testUser = sharedContext.getTestUser();
        sharedContext.clearTestMatches();

        for (int i = 0; i < count; i++) {
            Match match = Match.builder()
                    .version(0L)
                    .hostId(testUser.getId())
                    .hostNickname(testUser.getNickname())
                    .title("테스트 경기 " + (i + 1))
                    .description("테스트 경기 설명")
                    .latitude(BigDecimal.valueOf(37.5665))
                    .longitude(BigDecimal.valueOf(126.9780))
                    .address("서울시 중구")
                    .matchDate(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(18, 0))
                    .endTime(LocalTime.of(20, 0))
                    .maxParticipants(10)
                    .currentParticipants(0)
                    .status(MatchStatus.PENDING)
                    .build();
            Match savedMatch = matchRepository.save(match);
            sharedContext.addTestMatch(savedMatch);
        }
    }

    @그리고("사용자가 첫 번째 경기에 참가 신청했다")
    public void 사용자가_첫_번째_경기에_참가_신청했다() {
        User testUser = sharedContext.getTestUser();
        Match firstMatch = sharedContext.getTestMatches().get(0);

        Participation participation = Participation.builder()
                .matchId(firstMatch.getId())
                .userId(testUser.getId())
                .status(ParticipationStatus.CONFIRMED)
                .joinedAt(LocalDateTime.now())
                .build();
        participationRepository.save(participation);
    }

    @만일("내 참가 경기 목록 조회 API를 호출한다")
    public void 내_참가_경기_목록_조회_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();
        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth("/api/users/me/participations", accessToken);
        } else {
            response = testAdapter.get("/api/users/me/participations");
        }
        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 참가한 경기가 {int}개 포함되어 있다")
    public void 응답에_참가한_경기가_N개_포함되어_있다(int expectedCount) {
        TestResponse lastResponse = sharedContext.getLastResponse();
        int actualCount = lastResponse.getJsonArraySize();
        assertThat(actualCount)
                .as("참가한 경기 개수가 %d개 이어야 합니다", expectedCount)
                .isEqualTo(expectedCount);
    }
}
