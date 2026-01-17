package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.location.domain.Location;
import com.hoops.location.domain.repository.LocationRepository;
import com.hoops.user.domain.User;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchCreateStepDefs {

    private final TestAdapter testAdapter;
    private final LocationRepository locationRepository;
    private final SharedTestContext sharedContext;

    private Location testLocation;

    public MatchCreateStepDefs(
            TestAdapter testAdapter,
            LocationRepository locationRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.locationRepository = locationRepository;
        this.sharedContext = sharedContext;
    }

    @그리고("장소가 등록되어 있다")
    public void 장소가_등록되어_있다() {
        User testUser = sharedContext.getTestUser();
        Location location = Location.builder()
                .userId(testUser.getId())
                .alias("테스트 농구장")
                .latitude(BigDecimal.valueOf(37.5665))
                .longitude(BigDecimal.valueOf(126.9780))
                .address("서울특별시 중구 세종대로 110")
                .build();
        testLocation = locationRepository.save(location);
        sharedContext.setTestLocation(testLocation);
    }

    @만일("유효한 경기 정보로 경기 생성 API를 호출한다")
    public void 유효한_경기_정보로_경기_생성_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();

        Map<String, Object> request = createValidMatchRequest();

        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.postWithAuth("/api/matches", request, accessToken);
        } else {
            response = testAdapter.post("/api/matches", request);
        }
        sharedContext.setLastResponse(response);
    }

    @만일("제목 없이 경기 생성 API를 호출한다")
    public void 제목_없이_경기_생성_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();

        Map<String, Object> request = createValidMatchRequest();
        request.remove("title");

        TestResponse response = testAdapter.postWithAuth("/api/matches", request, accessToken);
        sharedContext.setLastResponse(response);
    }

    @만일("과거 날짜로 경기 생성 API를 호출한다")
    public void 과거_날짜로_경기_생성_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();

        Map<String, Object> request = createValidMatchRequest();
        request.put("matchDate", LocalDate.now().minusDays(1).toString());

        TestResponse response = testAdapter.postWithAuth("/api/matches", request, accessToken);
        sharedContext.setLastResponse(response);
    }

    @만일("최대 참가 인원 {int}명으로 경기 생성 API를 호출한다")
    public void 최대_참가_인원_N명으로_경기_생성_API를_호출한다(int maxParticipants) {
        String accessToken = sharedContext.getAccessToken();

        Map<String, Object> request = createValidMatchRequest();
        request.put("maxParticipants", maxParticipants);

        TestResponse response = testAdapter.postWithAuth("/api/matches", request, accessToken);
        sharedContext.setLastResponse(response);
    }

    @만일("종료 시간이 시작 시간보다 빠른 경기 생성 API를 호출한다")
    public void 종료_시간이_시작_시간보다_빠른_경기_생성_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();

        Map<String, Object> request = createValidMatchRequest();
        request.put("startTime", "20:00");
        request.put("endTime", "18:00");

        TestResponse response = testAdapter.postWithAuth("/api/matches", request, accessToken);
        sharedContext.setLastResponse(response);
    }

    @만일("존재하지 않는 장소로 경기 생성 API를 호출한다")
    public void 존재하지_않는_장소로_경기_생성_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();

        Map<String, Object> request = createValidMatchRequest();
        request.put("locationId", 999999L);

        TestResponse response = testAdapter.postWithAuth("/api/matches", request, accessToken);
        sharedContext.setLastResponse(response);
    }

    private Map<String, Object> createValidMatchRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("locationId", testLocation != null ? testLocation.getId() : 1L);
        request.put("title", "주말 농구 경기");
        request.put("description", "주말에 함께 농구해요!");
        request.put("matchDate", LocalDate.now().plusDays(7).toString());
        request.put("startTime", "18:00");
        request.put("endTime", "20:00");
        request.put("maxParticipants", 10);
        return request;
    }
}
