package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.location.domain.model.Location;
import com.hoops.location.domain.repository.LocationRepository;
import com.hoops.match.domain.repository.MatchRepository;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;
import com.hoops.participation.domain.model.Participation;
import com.hoops.participation.domain.vo.ParticipationStatus;
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
import java.util.HashMap;
import java.util.Map;

/**
 * P0 시간 겹침 제한 Step Definitions
 */
public class P0OverlapRestrictionStepDefs {

    private final TestAdapter testAdapter;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final LocationRepository locationRepository;
    private final SharedTestContext sharedContext;

    private Match secondMatch;
    private Location testLocation;

    public P0OverlapRestrictionStepDefs(
            TestAdapter testAdapter,
            MatchRepository matchRepository,
            UserRepository userRepository,
            ParticipationRepository participationRepository,
            LocationRepository locationRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.participationRepository = participationRepository;
        this.locationRepository = locationRepository;
        this.sharedContext = sharedContext;
    }

    @먼저("내일 {int}시부터 {int}시까지 진행되는 다른 사용자의 경기가 있다")
    public void 내일_시간대_다른_사용자의_경기가_있다(int startHour, int endHour) {
        User otherUser = createOtherUser("otherhost");
        sharedContext.setHostUser(otherUser);
        sharedContext.clearTestMatches();

        Match match = createMatch(
                otherUser,
                LocalDate.now().plusDays(1),
                LocalTime.of(startHour, 0),
                LocalTime.of(endHour, 0),
                "다른 사용자의 경기"
        );
        Match savedMatch = matchRepository.save(match);
        sharedContext.addTestMatch(savedMatch);
    }

    @먼저("내일 {int}시부터 {int}시까지 진행되는 내 경기가 있다")
    public void 내일_시간대_내_경기가_있다(int startHour, int endHour) {
        User testUser = sharedContext.getTestUser();
        sharedContext.clearTestMatches();

        Match match = createMatch(
                testUser,
                LocalDate.now().plusDays(1),
                LocalTime.of(startHour, 0),
                LocalTime.of(endHour, 0),
                "내 경기"
        );
        Match savedMatch = matchRepository.save(match);
        sharedContext.addTestMatch(savedMatch);
    }

    @그리고("내일 {int}시부터 {int}시까지 진행되는 또 다른 경기가 있다")
    public void 내일_시간대_또_다른_경기가_있다(int startHour, int endHour) {
        User otherUser = createOtherUser("anotherhost");

        secondMatch = createMatch(
                otherUser,
                LocalDate.now().plusDays(1),
                LocalTime.of(startHour, 0),
                LocalTime.of(endHour, 0),
                "또 다른 경기"
        );
        secondMatch = matchRepository.save(secondMatch);
    }

    @만일("두번째 경기에 참가 신청 API를 호출한다")
    public void 두번째_경기에_참가_신청_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();

        TestResponse response = testAdapter.postWithAuth(
                "/api/matches/" + secondMatch.getId() + "/participations",
                new HashMap<>(),
                accessToken
        );
        sharedContext.setLastResponse(response);
    }

    @만일("내일 {int}시부터 {int}시까지 경기 생성 API를 호출한다")
    public void 내일_시간대_경기_생성_API를_호출한다(int startHour, int endHour) {
        String accessToken = sharedContext.getAccessToken();

        ensureLocationExists();

        Map<String, Object> request = new HashMap<>();
        request.put("locationId", testLocation.getId());
        request.put("title", "새 경기");
        request.put("description", "테스트 경기");
        request.put("matchDate", LocalDate.now().plusDays(1).toString());
        request.put("startTime", String.format("%02d:00", startHour));
        request.put("endTime", String.format("%02d:00", endHour));
        request.put("maxParticipants", 10);

        TestResponse response = testAdapter.postWithAuth("/api/matches", request, accessToken);
        sharedContext.setLastResponse(response);
    }

    private Match createMatch(User host, LocalDate matchDate, LocalTime startTime, LocalTime endTime, String title) {
        return Match.builder()
                .version(0L)
                .hostId(host.getId())
                .hostNickname(host.getNickname())
                .title(title)
                .description("테스트 경기 설명")
                .latitude(BigDecimal.valueOf(37.5665))
                .longitude(BigDecimal.valueOf(126.9780))
                .address("서울시 중구")
                .matchDate(matchDate)
                .startTime(startTime)
                .endTime(endTime)
                .maxParticipants(10)
                .currentParticipants(1)
                .status(MatchStatus.PENDING)
                .build();
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

    private void ensureLocationExists() {
        if (testLocation == null) {
            User testUser = sharedContext.getTestUser();
            Location location = Location.builder()
                    .userId(testUser.getId())
                    .alias("테스트 농구장")
                    .latitude(BigDecimal.valueOf(37.5665))
                    .longitude(BigDecimal.valueOf(126.9780))
                    .address("서울특별시 중구 세종대로 110")
                    .build();
            testLocation = locationRepository.save(location);
        }
    }
}
