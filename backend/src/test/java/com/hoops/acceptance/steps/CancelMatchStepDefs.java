package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import com.hoops.match.domain.MatchStatus;
import com.hoops.user.domain.User;
import com.hoops.user.domain.repository.UserRepository;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CancelMatchStepDefs {

    private final TestAdapter testAdapter;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final SharedTestContext sharedContext;

    public CancelMatchStepDefs(
            TestAdapter testAdapter,
            MatchRepository matchRepository,
            UserRepository userRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.sharedContext = sharedContext;
    }

    @먼저("내가 생성한 경기가 있다")
    public void 내가_생성한_경기가_있다() {
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
                .currentParticipants(0)
                .status(MatchStatus.PENDING)
                .build();
        Match savedMatch = matchRepository.save(match);
        sharedContext.addTestMatch(savedMatch);
    }

    @먼저("다른 사용자가 생성한 경기가 있다")
    public void 다른_사용자가_생성한_경기가_있다() {
        User otherUser = User.builder()
                .email("other@example.com")
                .nickname("다른사용자")
                .rating(BigDecimal.valueOf(3.0))
                .totalMatches(0)
                .build();
        otherUser = userRepository.save(otherUser);

        sharedContext.clearTestMatches();

        Match match = Match.builder()
                .version(0L)
                .hostId(otherUser.getId())
                .hostNickname(otherUser.getNickname())
                .title("다른 사용자의 경기")
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

    @먼저("이미 시작된 경기가 있다")
    public void 이미_시작된_경기가_있다() {
        User testUser = sharedContext.getTestUser();
        sharedContext.clearTestMatches();

        Match match = Match.builder()
                .version(0L)
                .hostId(testUser.getId())
                .hostNickname(testUser.getNickname())
                .title("이미 시작된 경기")
                .description("테스트 경기 설명")
                .latitude(BigDecimal.valueOf(37.5665))
                .longitude(BigDecimal.valueOf(126.9780))
                .address("서울시 중구")
                .matchDate(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .maxParticipants(10)
                .currentParticipants(0)
                .status(MatchStatus.IN_PROGRESS)
                .build();
        Match savedMatch = matchRepository.save(match);
        sharedContext.addTestMatch(savedMatch);
    }

    @만일("해당 경기 취소 API를 호출한다")
    public void 해당_경기_취소_API를_호출한다() {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        Map<String, Object> requestBody = Map.of("reason", "테스트 취소 사유입니다");
        TestResponse response = testAdapter.deleteWithAuthAndBody(
                "/api/matches/" + match.getId(),
                requestBody,
                accessToken
        );
        sharedContext.setLastResponse(response);
    }

    @만일("존재하지 않는 경기 취소 API를 호출한다")
    public void 존재하지_않는_경기_취소_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();
        Long nonExistentMatchId = 999999L;

        Map<String, Object> requestBody = Map.of("reason", "테스트 취소 사유입니다");
        TestResponse response = testAdapter.deleteWithAuthAndBody(
                "/api/matches/" + nonExistentMatchId,
                requestBody,
                accessToken
        );
        sharedContext.setLastResponse(response);
    }

    @그리고("해당 경기의 상태가 {word} 이다")
    public void 해당_경기의_상태가_이다(String expectedStatus) {
        Match match = sharedContext.getTestMatches().get(0);
        Match updatedMatch = matchRepository.findById(match.getId())
                .orElseThrow(() -> new AssertionError("경기를 찾을 수 없습니다"));

        assertThat(updatedMatch.getStatus().name())
                .as("경기 상태가 %s 이어야 합니다", expectedStatus)
                .isEqualTo(expectedStatus);
    }
}
