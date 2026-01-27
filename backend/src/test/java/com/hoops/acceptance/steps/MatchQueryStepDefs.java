package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.match.application.port.out.MatchGeoIndexPort;
import com.hoops.match.application.port.out.MatchRepositoryPort;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;
import com.hoops.user.domain.model.User;
import com.hoops.user.application.port.out.UserRepositoryPort;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchQueryStepDefs {

    private final TestAdapter testAdapter;
    private final MatchRepositoryPort matchRepository;
    private final MatchGeoIndexPort matchGeoIndex;
    private final UserRepositoryPort userRepository;
    private final SharedTestContext sharedContext;

    // 서울 좌표
    private static final BigDecimal SEOUL_LATITUDE = BigDecimal.valueOf(37.5665);
    private static final BigDecimal SEOUL_LONGITUDE = BigDecimal.valueOf(126.9780);

    // 부산 좌표
    private static final BigDecimal BUSAN_LATITUDE = BigDecimal.valueOf(35.1796);
    private static final BigDecimal BUSAN_LONGITUDE = BigDecimal.valueOf(129.0756);

    public MatchQueryStepDefs(
            TestAdapter testAdapter,
            MatchRepositoryPort matchRepository,
            MatchGeoIndexPort matchGeoIndex,
            UserRepositoryPort userRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.matchRepository = matchRepository;
        this.matchGeoIndex = matchGeoIndex;
        this.userRepository = userRepository;
        this.sharedContext = sharedContext;
    }

    @먼저("서울 지역에 경기가 {int}개 등록되어 있다")
    public void 서울_지역에_경기가_N개_등록되어_있다(int count) {
        User testUser = sharedContext.getTestUser();
        if (testUser == null) {
            testUser = createTestUser();
            sharedContext.setTestUser(testUser);
        }

        sharedContext.clearTestMatches();

        for (int i = 0; i < count; i++) {
            Match match = createMatch(testUser, SEOUL_LATITUDE, SEOUL_LONGITUDE, "서울 경기 " + (i + 1));
            Match savedMatch = matchRepository.save(match);
            matchGeoIndex.addMatch(savedMatch.getId(), savedMatch.getLongitude(), savedMatch.getLatitude());
            sharedContext.addTestMatch(savedMatch);
        }
    }

    @먼저("부산 지역에 경기가 {int}개 등록되어 있다")
    public void 부산_지역에_경기가_N개_등록되어_있다(int count) {
        User testUser = sharedContext.getTestUser();
        if (testUser == null) {
            testUser = createTestUser();
            sharedContext.setTestUser(testUser);
        }

        sharedContext.clearTestMatches();

        for (int i = 0; i < count; i++) {
            Match match = createMatch(testUser, BUSAN_LATITUDE, BUSAN_LONGITUDE, "부산 경기 " + (i + 1));
            Match savedMatch = matchRepository.save(match);
            matchGeoIndex.addMatch(savedMatch.getId(), savedMatch.getLongitude(), savedMatch.getLatitude());
            sharedContext.addTestMatch(savedMatch);
        }
    }

    @만일("해당 경기 상세 조회 API를 호출한다")
    public void 해당_경기_상세_조회_API를_호출한다() {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth("/api/matches/" + match.getId(), accessToken);
        } else {
            response = testAdapter.get("/api/matches/" + match.getId());
        }
        sharedContext.setLastResponse(response);
    }

    @만일("존재하지 않는 경기 상세 조회 API를 호출한다")
    public void 존재하지_않는_경기_상세_조회_API를_호출한다() {
        Long nonExistentMatchId = 999999L;
        String accessToken = sharedContext.getAccessToken();

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth("/api/matches/" + nonExistentMatchId, accessToken);
        } else {
            response = testAdapter.get("/api/matches/" + nonExistentMatchId);
        }
        sharedContext.setLastResponse(response);
    }

    @만일("서울 중심으로 반경 {int}km 내 경기 목록을 조회한다")
    public void 서울_중심으로_반경_Nkm_내_경기_목록을_조회한다(int distanceKm) {
        String accessToken = sharedContext.getAccessToken();
        String path = String.format("/api/matches?latitude=%s&longitude=%s&distance=%d",
                SEOUL_LATITUDE, SEOUL_LONGITUDE, distanceKm);

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth(path, accessToken);
        } else {
            response = testAdapter.get(path);
        }
        sharedContext.setLastResponse(response);
    }

    @그리고("응답에 호스트 정보가 포함되어 있다")
    public void 응답에_호스트_정보가_포함되어_있다() {
        TestResponse response = sharedContext.getLastResponse();
        assertThat(response.hasJsonField("hostId"))
                .as("응답에 호스트 ID가 포함되어야 합니다")
                .isTrue();
        assertThat(response.hasJsonField("hostNickname"))
                .as("응답에 호스트 닉네임이 포함되어야 합니다")
                .isTrue();
    }

    @그리고("응답에 장소 정보가 포함되어 있다")
    public void 응답에_장소_정보가_포함되어_있다() {
        TestResponse response = sharedContext.getLastResponse();
        assertThat(response.hasJsonField("latitude"))
                .as("응답에 위도가 포함되어야 합니다")
                .isTrue();
        assertThat(response.hasJsonField("longitude"))
                .as("응답에 경도가 포함되어야 합니다")
                .isTrue();
        assertThat(response.hasJsonField("address"))
                .as("응답에 주소가 포함되어야 합니다")
                .isTrue();
    }

    @그리고("응답에 경기가 {int}개 포함되어 있다")
    public void 응답에_경기가_N개_포함되어_있다(int expectedCount) {
        TestResponse response = sharedContext.getLastResponse();
        int actualCount = response.getJsonArraySize();
        assertThat(actualCount)
                .as("응답에 경기가 %d개 포함되어야 합니다", expectedCount)
                .isEqualTo(expectedCount);
    }

    private User createTestUser() {
        User user = User.reconstitute(null,
                "querytest@example.com",
                "조회테스트유저",
                null,
                BigDecimal.valueOf(3.0),
                0);
        return userRepository.save(user);
    }

    private Match createMatch(User host, BigDecimal latitude, BigDecimal longitude, String title) {
        return Match.reconstitute(
                null,  // id
                0L,    // version
                host.getId(),  // hostId
                host.getNickname(),  // hostNickname
                title,  // title
                "테스트 경기 설명",  // description
                latitude,  // latitude
                longitude,  // longitude
                "테스트 주소",  // address
                LocalDate.now().plusDays(7),  // matchDate
                LocalTime.of(18, 0),  // startTime
                LocalTime.of(20, 0),  // endTime
                10,  // maxParticipants
                0,   // currentParticipants
                MatchStatus.PENDING,  // status
                null  // cancelledAt
        );
    }
}
