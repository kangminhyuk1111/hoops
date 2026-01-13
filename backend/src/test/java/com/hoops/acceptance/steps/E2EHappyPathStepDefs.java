package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.auth.application.port.out.JwtTokenProvider;
import com.hoops.location.domain.Location;
import com.hoops.location.domain.repository.LocationRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E Happy Path Step Definitions
 *
 * 경기 참가 전체 플로우를 테스트하기 위한 스텝 정의입니다.
 */
public class E2EHappyPathStepDefs {

    private final TestAdapter testAdapter;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final LocationRepository locationRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SharedTestContext sharedContext;

    private static final BigDecimal SEOUL_LATITUDE = BigDecimal.valueOf(37.5665);
    private static final BigDecimal SEOUL_LONGITUDE = BigDecimal.valueOf(126.9780);

    public E2EHappyPathStepDefs(
            TestAdapter testAdapter,
            UserRepository userRepository,
            MatchRepository matchRepository,
            LocationRepository locationRepository,
            JwtTokenProvider jwtTokenProvider,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.locationRepository = locationRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.sharedContext = sharedContext;
    }

    @그리고("호스트가 서울 지역에 경기를 생성했다")
    public void 호스트가_서울_지역에_경기를_생성했다() {
        User host = sharedContext.getHostUser();

        Location location = new Location(
                null,
                host.getId(),
                "서울 테스트 농구장",
                SEOUL_LATITUDE,
                SEOUL_LONGITUDE,
                "서울시 강남구 테스트동 123"
        );
        locationRepository.save(location);

        LocalDate matchDate = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(18, 0);
        LocalTime endTime = LocalTime.of(20, 0);

        Match match = new Match(
                null,
                0L,
                host.getId(),
                host.getNickname(),
                "서울 주말 농구",
                "함께 농구해요!",
                SEOUL_LATITUDE,
                SEOUL_LONGITUDE,
                location.getAddress(),
                matchDate,
                startTime,
                endTime,
                10,
                1,
                MatchStatus.PENDING,
                null
        );
        Match savedMatch = matchRepository.save(match);
        sharedContext.clearTestMatches();
        sharedContext.addTestMatch(savedMatch);

        // 호스트 토큰 저장
        sharedContext.setHostAccessToken(sharedContext.getAccessToken());
    }

    // "사용자가 회원가입되어 있다"와 "사용자가 로그인되어 있다"는
    // UserProfileStepDefs에서 정의되어 있으므로 여기서는 생략합니다.
    // 단, E2E 테스트에서 사용자 토큰을 별도로 저장해야 하는 경우를 처리합니다.

    @그리고("사용자 토큰을 저장한다")
    public void 사용자_토큰을_저장한다() {
        String accessToken = sharedContext.getAccessToken();
        sharedContext.setUserAccessToken(accessToken);
    }

    @그리고("응답에 경기가 {int}개 이상 포함되어 있다")
    public void 응답에_경기가_N개_이상_포함되어_있다(int minCount) {
        TestResponse response = sharedContext.getLastResponse();
        int actualCount = response.getJsonArraySize();
        assertThat(actualCount)
                .as("응답에 경기가 최소 %d개 포함되어야 합니다", minCount)
                .isGreaterThanOrEqualTo(minCount);
    }

    @그리고("사용자의 참가 상태가 {string} 또는 {string} 이다")
    public void 사용자의_참가_상태가_A_또는_B_이다(String status1, String status2) {
        TestResponse response = sharedContext.getLastResponse();
        String actualStatus = (String) response.getJsonValue("status");

        // 참가 ID 저장
        Object participationId = response.getJsonValue("id");
        if (participationId != null) {
            sharedContext.setParticipationId(((Number) participationId).longValue());
        }

        assertThat(actualStatus)
                .as("참가 상태가 %s 또는 %s 이어야 합니다", status1, status2)
                .isIn(status1, status2);
    }

    @먼저("호스트가 다시 로그인한다")
    public void 호스트가_다시_로그인한다() {
        String hostToken = sharedContext.getHostAccessToken();
        if (hostToken != null) {
            sharedContext.setAccessToken(hostToken);
        } else {
            User host = sharedContext.getHostUser();
            String accessToken = jwtTokenProvider.createTokens(host.getId()).accessToken();
            sharedContext.setAccessToken(accessToken);
            sharedContext.setHostAccessToken(accessToken);
        }
    }

    @만일("호스트가 경기의 참가자 목록을 조회한다")
    public void 호스트가_경기의_참가자_목록을_조회한다() {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        String path = "/api/matches/" + match.getId() + "/participants";
        TestResponse response = testAdapter.getWithAuth(path, accessToken);
        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 참가 신청자가 포함되어 있다")
    public void 응답에_참가_신청자가_포함되어_있다() {
        TestResponse response = sharedContext.getLastResponse();
        int participantCount = response.getJsonArraySize();
        assertThat(participantCount)
                .as("참가 신청자가 최소 1명 이상 있어야 합니다")
                .isGreaterThanOrEqualTo(1);

        // 첫 번째 참가자의 ID 저장 (승인에 사용)
        @SuppressWarnings("unchecked")
        List<Object> participants = (List<Object>) response.getJsonValue("");
        if (participants != null && !participants.isEmpty()) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> firstParticipant = (java.util.Map<String, Object>) participants.get(0);
            Object participationId = firstParticipant.get("participationId");
            if (participationId == null) {
                participationId = firstParticipant.get("id");
            }
            if (participationId != null) {
                sharedContext.setParticipationId(((Number) participationId).longValue());
            }
        }
    }

    @만일("호스트가 해당 사용자의 참가를 승인한다")
    public void 호스트가_해당_사용자의_참가를_승인한다() {
        Match match = sharedContext.getTestMatches().get(0);
        Long participationId = sharedContext.getParticipationId();
        String accessToken = sharedContext.getAccessToken();

        String path = "/api/matches/" + match.getId() + "/participations/" + participationId + "/approve";
        TestResponse response = testAdapter.putWithAuth(path, null, accessToken);
        sharedContext.setLastResponse(response);
    }

    @먼저("사용자가 다시 로그인한다")
    public void 사용자가_다시_로그인한다() {
        String userToken = sharedContext.getUserAccessToken();
        if (userToken != null) {
            sharedContext.setAccessToken(userToken);
        } else {
            User user = sharedContext.getTestUser();
            String accessToken = jwtTokenProvider.createTokens(user.getId()).accessToken();
            sharedContext.setAccessToken(accessToken);
            sharedContext.setUserAccessToken(accessToken);
        }
    }

    @만일("내 참가 경기 목록을 조회한다")
    public void 내_참가_경기_목록을_조회한다() {
        String accessToken = sharedContext.getAccessToken();
        TestResponse response = testAdapter.getWithAuth("/api/users/me/participations", accessToken);
        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 참가한 경기가 포함되어 있다")
    public void 응답에_참가한_경기가_포함되어_있다() {
        TestResponse response = sharedContext.getLastResponse();
        int participationCount = response.getJsonArraySize();
        assertThat(participationCount)
                .as("참가한 경기가 최소 1개 이상 있어야 합니다")
                .isGreaterThanOrEqualTo(1);
    }
}
