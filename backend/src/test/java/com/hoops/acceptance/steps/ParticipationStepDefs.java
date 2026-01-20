package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.match.domain.repository.MatchRepository;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;
import com.hoops.participation.domain.Participation;
import com.hoops.participation.domain.ParticipationStatus;
import com.hoops.participation.domain.repository.ParticipationRepository;
import com.hoops.user.domain.model.User;
import com.hoops.user.domain.repository.UserRepository;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 경기 참가 신청 Step Definitions
 */
public class ParticipationStepDefs {

    private final TestAdapter testAdapter;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final SharedTestContext sharedContext;

    private Participation testParticipation;

    public ParticipationStepDefs(
            TestAdapter testAdapter,
            MatchRepository matchRepository,
            UserRepository userRepository,
            ParticipationRepository participationRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.participationRepository = participationRepository;
        this.sharedContext = sharedContext;
    }

    @그리고("이미 해당 경기에 참가 신청을 했다")
    public void 이미_해당_경기에_참가_신청을_했다() {
        User testUser = sharedContext.getTestUser();
        Match match = sharedContext.getTestMatches().get(0);

        Participation participation = Participation.builder()
                .matchId(match.getId())
                .userId(testUser.getId())
                .status(ParticipationStatus.PENDING)
                .joinedAt(LocalDateTime.now())
                .build();
        testParticipation = participationRepository.save(participation);
    }

    @그리고("해당 참가를 취소했다")
    public void 해당_참가를_취소했다() {
        Participation cancelledParticipation = testParticipation.cancel();
        testParticipation = participationRepository.save(cancelledParticipation);
    }

    @먼저("정원이 찬 경기가 있다")
    public void 정원이_찬_경기가_있다() {
        User otherUser = createOtherUser("fullmatch");
        sharedContext.clearTestMatches();

        Match match = Match.builder()
                .version(0L)
                .hostId(otherUser.getId())
                .hostNickname(otherUser.getNickname())
                .title("정원 찬 경기")
                .description("테스트 경기 설명")
                .latitude(BigDecimal.valueOf(37.5665))
                .longitude(BigDecimal.valueOf(126.9780))
                .address("서울시 중구")
                .matchDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .maxParticipants(4)
                .currentParticipants(4)
                .status(MatchStatus.PENDING)
                .build();
        Match savedMatch = matchRepository.save(match);
        sharedContext.addTestMatch(savedMatch);
    }

    @먼저("이미 시작된 다른 사용자의 경기가 있다")
    public void 이미_시작된_다른_사용자의_경기가_있다() {
        User otherUser = createOtherUser("started");
        sharedContext.clearTestMatches();

        Match match = Match.builder()
                .version(0L)
                .hostId(otherUser.getId())
                .hostNickname(otherUser.getNickname())
                .title("이미 시작된 경기")
                .description("테스트 경기 설명")
                .latitude(BigDecimal.valueOf(37.5665))
                .longitude(BigDecimal.valueOf(126.9780))
                .address("서울시 중구")
                .matchDate(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .maxParticipants(10)
                .currentParticipants(2)
                .status(MatchStatus.IN_PROGRESS)
                .build();
        Match savedMatch = matchRepository.save(match);
        sharedContext.addTestMatch(savedMatch);
    }

    @만일("해당 경기에 참가 신청 API를 호출한다")
    public void 해당_경기에_참가_신청_API를_호출한다() {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        String path = "/api/matches/" + match.getId() + "/participations";
        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.postWithAuth(path, null, accessToken);
        } else {
            response = testAdapter.post(path, null);
        }
        sharedContext.setLastResponse(response);
    }

    @만일("존재하지 않는 경기에 참가 신청 API를 호출한다")
    public void 존재하지_않는_경기에_참가_신청_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();
        Long nonExistentMatchId = 999999L;

        String path = "/api/matches/" + nonExistentMatchId + "/participations";
        TestResponse response = testAdapter.postWithAuth(path, null, accessToken);
        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 참가 ID가 포함되어 있다")
    public void 응답에_참가_ID가_포함되어_있다() {
        TestResponse response = sharedContext.getLastResponse();
        assertThat(response.hasJsonField("id"))
                .as("응답에 참가 ID가 포함되어야 합니다")
                .isTrue();
    }

    @그리고("응답의 참가 상태가 {string} 이다")
    public void 응답의_참가_상태가_이다(String expectedStatus) {
        TestResponse response = sharedContext.getLastResponse();
        assertThat(response.getJsonValue("status"))
                .as("응답의 참가 상태가 %s 이어야 합니다", expectedStatus)
                .isEqualTo(expectedStatus);
    }

    private User createOtherUser(String prefix) {
        User user = User.builder()
                .email(prefix + System.currentTimeMillis() + "@example.com")
                .nickname(prefix + "사용자")
                .rating(BigDecimal.valueOf(3.0))
                .totalMatches(0)
                .build();
        return userRepository.save(user);
    }
}
