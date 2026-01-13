package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.location.domain.Location;
import com.hoops.location.domain.repository.LocationRepository;
import com.hoops.user.domain.User;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * P0 경기 생성 제한 Step Definitions
 */
public class P0MatchCreationLimitStepDefs {

    private final TestAdapter testAdapter;
    private final LocationRepository locationRepository;
    private final SharedTestContext sharedContext;

    private Location testLocation;

    public P0MatchCreationLimitStepDefs(
            TestAdapter testAdapter,
            LocationRepository locationRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.locationRepository = locationRepository;
        this.sharedContext = sharedContext;
    }

    @만일("{int}시간 후에 시작하는 경기 생성 API를 호출한다")
    public void N시간_후에_시작하는_경기_생성_API를_호출한다(int hoursLater) {
        String accessToken = sharedContext.getAccessToken();
        ensureLocationExists();

        LocalDateTime startDateTime = LocalDateTime.now().plusHours(hoursLater);
        LocalDate matchDate = startDateTime.toLocalDate();
        LocalTime startTime = startDateTime.toLocalTime();
        LocalTime endTime = startTime.plusHours(2);

        Map<String, Object> request = createMatchRequest(matchDate, startTime, endTime, 10);

        TestResponse response = testAdapter.postWithAuth("/api/matches", request, accessToken);
        sharedContext.setLastResponse(response);
    }

    @만일("{int}분 후에 시작하는 경기 생성 API를 호출한다")
    public void N분_후에_시작하는_경기_생성_API를_호출한다(int minutesLater) {
        String accessToken = sharedContext.getAccessToken();
        ensureLocationExists();

        LocalDateTime startDateTime = LocalDateTime.now().plusMinutes(minutesLater);
        LocalDate matchDate = startDateTime.toLocalDate();
        LocalTime startTime = startDateTime.toLocalTime();
        LocalTime endTime = startTime.plusHours(2);

        Map<String, Object> request = createMatchRequest(matchDate, startTime, endTime, 10);

        TestResponse response = testAdapter.postWithAuth("/api/matches", request, accessToken);
        sharedContext.setLastResponse(response);
    }

    @만일("{int}일 후에 시작하는 경기 생성 API를 호출한다")
    public void N일_후에_시작하는_경기_생성_API를_호출한다(int daysLater) {
        String accessToken = sharedContext.getAccessToken();
        ensureLocationExists();

        LocalDate matchDate = LocalDate.now().plusDays(daysLater);
        LocalTime startTime = LocalTime.of(14, 0);
        LocalTime endTime = LocalTime.of(16, 0);

        Map<String, Object> request = createMatchRequest(matchDate, startTime, endTime, 10);

        TestResponse response = testAdapter.postWithAuth("/api/matches", request, accessToken);
        sharedContext.setLastResponse(response);
    }

    private Map<String, Object> createMatchRequest(LocalDate matchDate, LocalTime startTime, LocalTime endTime, int maxParticipants) {
        Map<String, Object> request = new HashMap<>();
        request.put("locationId", testLocation.getId());
        request.put("title", "테스트 경기");
        request.put("description", "테스트 경기 설명");
        request.put("matchDate", matchDate.toString());
        request.put("startTime", startTime.toString().substring(0, 5));
        request.put("endTime", endTime.toString().substring(0, 5));
        request.put("maxParticipants", maxParticipants);
        return request;
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
