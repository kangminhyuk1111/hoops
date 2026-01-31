package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.match.application.port.out.MatchGeoIndexPort;
import com.hoops.match.application.port.out.MatchRepositoryPort;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;
import com.hoops.user.application.port.out.UserRepositoryPort;
import com.hoops.user.domain.model.User;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchRecruitmentStatusStepDefs {

    private final TestAdapter testAdapter;
    private final MatchRepositoryPort matchRepository;
    private final MatchGeoIndexPort matchGeoIndex;
    private final UserRepositoryPort userRepository;
    private final SharedTestContext sharedContext;

    private static final BigDecimal SEOUL_LATITUDE = BigDecimal.valueOf(37.5665);
    private static final BigDecimal SEOUL_LONGITUDE = BigDecimal.valueOf(126.9780);

    public MatchRecruitmentStatusStepDefs(
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

    @먼저("서울 지역에 최대 {int}명, 현재 {int}명인 경기가 등록되어 있다")
    public void 서울_지역에_최대_N명_현재_M명인_경기가_등록되어_있다(int maxParticipants, int currentParticipants) {
        User testUser = ensureTestUser();
        sharedContext.clearTestMatches();

        MatchStatus status = currentParticipants >= maxParticipants ? MatchStatus.FULL : MatchStatus.PENDING;
        Match match = createMatchWithParticipants(testUser, SEOUL_LATITUDE, SEOUL_LONGITUDE,
                "모집현황 테스트 경기", maxParticipants, currentParticipants, status);
        Match savedMatch = matchRepository.save(match);
        matchGeoIndex.addMatch(savedMatch.getId(), savedMatch.getLongitude(), savedMatch.getLatitude());
        sharedContext.addTestMatch(savedMatch);
    }

    @먼저("서울 지역에 {word} 상태 경기가 {int}개 등록되어 있다")
    public void 서울_지역에_상태_경기가_N개_등록되어_있다(String statusName, int count) {
        User testUser = ensureTestUser();
        MatchStatus matchStatus = MatchStatus.valueOf(statusName);

        int currentParticipants = matchStatus == MatchStatus.FULL ? 10 : 3;
        int maxParticipants = 10;

        for (int i = 0; i < count; i++) {
            Match match = createMatchWithParticipants(testUser, SEOUL_LATITUDE, SEOUL_LONGITUDE,
                    statusName + " 경기 " + (i + 1), maxParticipants, currentParticipants, matchStatus);
            Match savedMatch = matchRepository.save(match);
            matchGeoIndex.addMatch(savedMatch.getId(), savedMatch.getLongitude(), savedMatch.getLatitude());
            sharedContext.addTestMatch(savedMatch);
        }
    }

    @먼저("서울 지역에 남은 자리가 각각 {int}, {int}, {int}인 경기 {int}개가 등록되어 있다")
    public void 서울_지역에_남은_자리가_각각인_경기_N개가_등록되어_있다(int r1, int r2, int r3, int count) {
        User testUser = ensureTestUser();
        sharedContext.clearTestMatches();

        int[] remainingSlots = {r1, r2, r3};
        int maxParticipants = 10;

        for (int i = 0; i < count; i++) {
            int current = maxParticipants - remainingSlots[i];
            Match match = createMatchWithParticipants(testUser, SEOUL_LATITUDE, SEOUL_LONGITUDE,
                    "정렬 테스트 경기 " + (i + 1), maxParticipants, current, MatchStatus.PENDING);
            Match savedMatch = matchRepository.save(match);
            matchGeoIndex.addMatch(savedMatch.getId(), savedMatch.getLongitude(), savedMatch.getLatitude());
            sharedContext.addTestMatch(savedMatch);
        }
    }

    @만일("서울 중심으로 반경 {int}km 내 PENDING 상태 경기 목록을 조회한다")
    public void 서울_중심으로_반경_Nkm_내_PENDING_상태_경기_목록을_조회한다(int distanceKm) {
        String accessToken = sharedContext.getAccessToken();
        String path = String.format("/api/matches?latitude=%s&longitude=%s&distance=%d&status=PENDING",
                SEOUL_LATITUDE, SEOUL_LONGITUDE, distanceKm);

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth(path, accessToken);
        } else {
            response = testAdapter.get(path);
        }
        sharedContext.setLastResponse(response);
    }

    @만일("서울 중심으로 반경 {int}km 내 경기 목록을 마감 임박순으로 조회한다")
    public void 서울_중심으로_반경_Nkm_내_경기_목록을_마감_임박순으로_조회한다(int distanceKm) {
        String accessToken = sharedContext.getAccessToken();
        String path = String.format("/api/matches?latitude=%s&longitude=%s&distance=%d&sort=URGENCY",
                SEOUL_LATITUDE, SEOUL_LONGITUDE, distanceKm);

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth(path, accessToken);
        } else {
            response = testAdapter.get(path);
        }
        sharedContext.setLastResponse(response);
    }

    @만일("서울 중심으로 반경 {int}km 내 경기 목록을 잘못된 정렬값으로 조회한다")
    public void 서울_중심으로_반경_Nkm_내_경기_목록을_잘못된_정렬값으로_조회한다(int distanceKm) {
        String accessToken = sharedContext.getAccessToken();
        String path = String.format("/api/matches?latitude=%s&longitude=%s&distance=%d&sort=INVALID",
                SEOUL_LATITUDE, SEOUL_LONGITUDE, distanceKm);

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.getWithAuth(path, accessToken);
        } else {
            response = testAdapter.get(path);
        }
        sharedContext.setLastResponse(response);
    }

    @그리고("첫 번째 경기의 남은 자리가 {int} 이다")
    public void 첫_번째_경기의_남은_자리가_N_이다(int expectedRemainingSlots) {
        TestResponse response = sharedContext.getLastResponse();
        List<Map<String, Object>> matches = getMatchItems(response);
        assertThat(matches).isNotEmpty();
        assertThat(((Number) matches.get(0).get("remainingSlots")).intValue())
                .as("첫 번째 경기의 남은 자리가 %d 이어야 합니다", expectedRemainingSlots)
                .isEqualTo(expectedRemainingSlots);
    }

    @그리고("첫 번째 경기의 모집 상태가 {string} 이다")
    public void 첫_번째_경기의_모집_상태가_이다(String expectedStatus) {
        TestResponse response = sharedContext.getLastResponse();
        List<Map<String, Object>> matches = getMatchItems(response);
        assertThat(matches).isNotEmpty();
        assertThat(matches.get(0).get("recruitmentStatus"))
                .as("첫 번째 경기의 모집 상태가 %s 이어야 합니다", expectedStatus)
                .isEqualTo(expectedStatus);
    }

    @그리고("경기 목록이 남은 자리 오름차순으로 정렬되어 있다")
    public void 경기_목록이_남은_자리_오름차순으로_정렬되어_있다() {
        TestResponse response = sharedContext.getLastResponse();
        List<Map<String, Object>> matches = getMatchItems(response);
        assertThat(matches.size()).isGreaterThanOrEqualTo(2);

        for (int i = 0; i < matches.size() - 1; i++) {
            int current = ((Number) matches.get(i).get("remainingSlots")).intValue();
            int next = ((Number) matches.get(i + 1).get("remainingSlots")).intValue();
            assertThat(current)
                    .as("경기 목록이 남은 자리 오름차순으로 정렬되어야 합니다 (index %d: %d, index %d: %d)", i, current, i + 1, next)
                    .isLessThanOrEqualTo(next);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getMatchItems(TestResponse response) {
        Object items = response.getJsonValue("items");
        if (items instanceof List) {
            return (List<Map<String, Object>>) items;
        }
        return response.getJsonList("$");
    }

    private User ensureTestUser() {
        User testUser = sharedContext.getTestUser();
        if (testUser == null) {
            testUser = User.reconstitute(null,
                    "recruitment-test@example.com",
                    "모집현황테스트유저",
                    null,
                    BigDecimal.valueOf(3.0),
                    0);
            testUser = userRepository.save(testUser);
            sharedContext.setTestUser(testUser);
        }
        return testUser;
    }

    private Match createMatchWithParticipants(User host, BigDecimal latitude, BigDecimal longitude,
                                               String title, int maxParticipants, int currentParticipants,
                                               MatchStatus status) {
        return Match.reconstitute(
                null,
                0L,
                host.getId(),
                host.getNickname(),
                title,
                "모집현황 테스트 설명",
                latitude,
                longitude,
                "테스트 주소",
                LocalDate.now().plusDays(7),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                maxParticipants,
                currentParticipants,
                status,
                null
        );
    }
}
