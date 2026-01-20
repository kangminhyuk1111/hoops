package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.location.domain.model.Location;
import com.hoops.location.domain.repository.LocationRepository;
import com.hoops.match.domain.repository.MatchRepository;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;
import com.hoops.user.domain.model.User;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 내가 생성한 경기 목록 조회 Step Definitions
 */
public class MyHostedMatchesStepDefs {

    private final TestAdapter testAdapter;
    private final MatchRepository matchRepository;
    private final LocationRepository locationRepository;
    private final SharedTestContext sharedContext;

    public MyHostedMatchesStepDefs(
            TestAdapter testAdapter,
            MatchRepository matchRepository,
            LocationRepository locationRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.matchRepository = matchRepository;
        this.locationRepository = locationRepository;
        this.sharedContext = sharedContext;
    }

    @먼저("내가 생성한 경기가 {int}개 있다")
    public void 내가_생성한_경기가_N개_있다(int count) {
        User user = sharedContext.getTestUser();

        Location location = new Location(
                null,
                user.getId(),
                "테스트 농구장",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                "서울시 강남구 테스트동 123"
        );
        Location savedLocation = locationRepository.save(location);

        LocalDate matchDate = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(18, 0);
        LocalTime endTime = LocalTime.of(20, 0);

        for (int i = 0; i < count; i++) {
            Match match = new Match(
                    null,
                    null,
                    user.getId(),
                    user.getNickname(),
                    "테스트 경기 " + (i + 1),
                    "테스트 경기입니다",
                    savedLocation.getLatitude(),
                    savedLocation.getLongitude(),
                    savedLocation.getAddress(),
                    matchDate.plusDays(i),
                    startTime,
                    endTime,
                    10,
                    1,
                    MatchStatus.PENDING,
                    null
            );
            Match savedMatch = matchRepository.save(match);
            sharedContext.addTestMatch(savedMatch);
        }
    }

    @만일("내가 호스팅한 경기 목록을 조회한다")
    public void 내가_호스팅한_경기_목록을_조회한다() {
        String accessToken = sharedContext.getAccessToken();
        TestResponse response;

        if (accessToken != null) {
            response = testAdapter.getWithAuth("/api/matches/hosted", accessToken);
        } else {
            response = testAdapter.get("/api/matches/hosted");
        }

        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 {int}개의 경기가 포함되어 있다")
    public void 응답에_N개의_경기가_포함되어_있다(int expectedCount) {
        TestResponse lastResponse = sharedContext.getLastResponse();
        int actualCount = lastResponse.getJsonArraySize();
        assertThat(actualCount)
                .as("응답에 %d개의 경기가 포함되어 있어야 합니다", expectedCount)
                .isEqualTo(expectedCount);
    }
}
